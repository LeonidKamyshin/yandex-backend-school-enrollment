package com.yandex.enrollment.api.service;

import com.yandex.enrollment.api.controller.ShopUnitController;
import com.yandex.enrollment.api.model.error.ErrorType;
import com.yandex.enrollment.api.model.shop.ShopUnit;
import com.yandex.enrollment.api.model.shop.ShopUnitImportRequest;
import com.yandex.enrollment.api.model.result.ValidationResult;
import com.yandex.enrollment.api.model.shop.ShopUnitType;
import com.yandex.enrollment.api.repository.ShopUnitRepository;
import com.yandex.enrollment.api.utils.DateUtils;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.validation.constraints.Null;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.DateOperators.TemporalUnit;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

@Service
public class ShopUnitValidationService {

  private static final Logger LOGGER = LogManager.getLogger(ShopUnitController.class);
  private static final String PATTERN_FORMAT = "dd.MM.yyyy";

  private final ShopUnitRepository repository;
  private final ShopUnitConverterService converterService;

  @Autowired
  public ShopUnitValidationService(ShopUnitRepository repository,
      ShopUnitConverterService converterService) {
    this.repository = repository;
    this.converterService = converterService;
  }

  public ValidationResult<Collection<ShopUnit>> validateShopUnitImportRequest(
      ShopUnitImportRequest request) {

    Collection<ShopUnit> shopUnits = converterService.convertShopUnitImportRequest(request);
    LOGGER.info("Сконвертированные обьекты: " + shopUnits);
    LOGGER.info("Проверка 1 " + checkTypeMatchRowsWithSameId().test(shopUnits));
    LOGGER.info("Проверка 2 " + checkIdsUnique().test(shopUnits));
    LOGGER.info("Проверка 3 " + checkParentType().test(shopUnits));
    LOGGER.info("Проверка 4 " + checkCategoryPrice().test(shopUnits));
    LOGGER.info("Проверка 5 " + checkOfferPrice().test(shopUnits));
    LOGGER.info("Проверка 6 " + checkDateFormat().test(shopUnits));
    LOGGER.info("Проверка 7 " + checkUUIDFormat().test(shopUnits));
    LOGGER.info("Проверка 8 " + checkParentExists().test(shopUnits));

    ValidationResult<Collection<ShopUnit>> result = new ValidationResult<>(shopUnits);
    if (!checkCorrect().test(shopUnits)) {
      result.addError(ErrorType.VALIDATION_FAILED_ERROR.getError());
    }

    return result;
  }

  /**
   * Создает предикат на полную валидацию запроса на импорт
   *
   * @return предикат, значение которого == валидность запроса
   */
  private Predicate<Collection<ShopUnit>> checkCorrect() {
    return checkTypeMatchRowsWithSameId()
        .and(checkIdsUnique())
        .and(checkParentType())
        .and(checkCategoryPrice())
        .and(checkOfferPrice())
        .and(checkDateFormat())
        .and(checkUUIDFormat())
        .and(checkParentExists());
  }

  /**
   * Валидация, что uuid товара или категории является уникальным среди товаров и категорий
   *
   * @return true, если uuid товара или категории является уникальным среди товаров и категорий
   */
  private Predicate<Collection<ShopUnit>> checkTypeMatchRowsWithSameId() {
    return shopUnits -> {
      List<String> ids = shopUnits.stream().map(ShopUnit::getId)
          .filter(Objects::nonNull).toList();
      Map<String, ShopUnitType> repoShopUnitTypeById = repository.findAllWithoutChildrenIdByIdIn(
              ids)
          .stream().collect(Collectors.toMap(ShopUnit::getId, ShopUnit::getType));

      return shopUnits.stream().noneMatch(
          shopUnit -> repoShopUnitTypeById.getOrDefault(shopUnit.getId(), shopUnit.getType())
              != shopUnit.getType()
      );
    };
  }

  /**
   * Валидация, что родителем товара или категории может быть только категория
   *
   * @return true если валидно
   */
  private Predicate<Collection<ShopUnit>> checkParentType() {
    return shopUnits -> {
      List<String> ids =
          shopUnits.stream().map(ShopUnit::getParentId).filter(Objects::nonNull).toList();
      Set<String> offerShopUnitIds = shopUnits.stream()
          .filter(shopUnit -> shopUnit.getType() == ShopUnitType.OFFER)
          .map(ShopUnit::getId)
          .collect(Collectors.toSet());
      return shopUnits.stream().noneMatch(
          shopUnit -> offerShopUnitIds.contains(shopUnit.getParentId()))
          && repository.countByIdInAndType(ids, ShopUnitType.OFFER) == 0;
    };
  }

  /**
   * Валидация, что цена категории null
   *
   * @return true если валидно
   */
  private Predicate<Collection<ShopUnit>> checkCategoryPrice() {
    return shopUnits -> shopUnits.stream()
        .noneMatch(shopUnit -> shopUnit.getType().equals(ShopUnitType.CATEGORY)
            && !Objects.isNull(shopUnit.getPrice()));
  }

  /**
   * Валидация, что цена оффера не null
   *
   * @return true если валидно
   */
  private Predicate<Collection<ShopUnit>> checkOfferPrice() {
    return shopUnits -> shopUnits.stream()
        .noneMatch(shopUnit -> shopUnit.getType().equals(ShopUnitType.OFFER)
            && (Objects.isNull(shopUnit.getPrice()) || shopUnit.getPrice() < 0L));
  }

  /**
   * Валидация, что в одном запросе не может быть двух элементов с одинаковым id
   *
   * @return true если валидно
   */
  private Predicate<Collection<ShopUnit>> checkIdsUnique() {
    return shopUnits -> shopUnits.stream().distinct().count() == shopUnits.size();
  }

  /**
   * Валидация, что дата в формате ISO 860
   *
   * @return true если валидно
   */
  private Predicate<Collection<ShopUnit>> checkDateFormat() {
    return shopUnits -> shopUnits.stream()
        .noneMatch(shopUnit -> {
          try {
            Instant.from(DateTimeFormatter.ISO_INSTANT.parse(shopUnit.getDate()));
            return false;
          } catch (DateTimeParseException | NullPointerException e) {
            LOGGER.info("Incorrect date in ShopUnitImportRequest");
            return true;
          }
        });
  }

  /**
   * Валидация, что id в формате UUID
   *
   * @return true если валидно
   */
  @SuppressWarnings("ResultOfMethodCallIgnored")
  private Predicate<Collection<ShopUnit>> checkUUIDFormat() {
    return shopUnits -> shopUnits.stream()
        .noneMatch(shopUnit -> {
          try {
            if (shopUnit.getParentId() != null) {
              UUID.fromString(shopUnit.getParentId());
            }
            UUID.fromString(shopUnit.getId());
            return false;
          } catch (IllegalArgumentException e) {
            LOGGER.info("Incorrect date in ShopUnitImportRequest");
            return true;
          }
        });
  }

  private Predicate<Collection<ShopUnit>> checkParentExists() {
    return shopUnits -> {
      Map<String, ShopUnit> shopUnitsById = shopUnits.stream().collect(Collectors
          .toMap(ShopUnit::getId, shopUnit -> shopUnit, (a, b) -> a, HashMap::new));

      List<String> checkUnitsParents = shopUnits.stream().filter(shopUnit ->
              shopUnit.getParentId() != null && !shopUnitsById.containsKey(shopUnit.getParentId()))
          .map(ShopUnit::getParentId)
          .toList();
      return repository.countByIdIn(checkUnitsParents) == checkUnitsParents.size();
    };
  }

  public ValidationResult<String> validateSalesDate(String date) {
    if (checkDateFormat().test(Collections.singleton(ShopUnit.builder().date(date).build()))) {
      String dateStart = DateUtils.dateToString(DateUtils.stringToDate(date)
          .minus(1, ChronoUnit.DAYS));
      return new ValidationResult<>(dateStart);
    } else {
      return new ValidationResult<>(ErrorType.VALIDATION_FAILED_ERROR.getError());
    }
  }
}
