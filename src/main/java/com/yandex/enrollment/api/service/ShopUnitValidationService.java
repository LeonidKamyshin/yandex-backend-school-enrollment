package com.yandex.enrollment.api.service;

import com.yandex.enrollment.api.controller.ShopUnitController;
import com.yandex.enrollment.api.model.error.Error;
import com.yandex.enrollment.api.model.error.ErrorType;
import com.yandex.enrollment.api.model.shop.ShopUnit;
import com.yandex.enrollment.api.model.shop.ShopUnitImportRequest;
import com.yandex.enrollment.api.model.result.ValidationResult;
import com.yandex.enrollment.api.model.shop.ShopUnitType;
import com.yandex.enrollment.api.repository.ShopUnitRepository;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShopUnitValidationService {

  private static final Logger LOGGER = LogManager.getLogger(ShopUnitController.class);

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
    ValidationResult<Collection<ShopUnit>> result = new ValidationResult<>(shopUnits);

    if (!checkCorrect().test(shopUnits)) {
      result.addError(ErrorType.VALIDATION_FAILED_ERROR.getError());
    }

    return new ValidationResult<>(shopUnits);
  }

  private Predicate<Collection<ShopUnit>> checkCorrect() {
    return checkTypeMatchRowsWithSameId()
        .and(checkIdsUnique())
        .and(checkParentType())
        .and(checkCategoryPrice())
        .and(checkOfferPrice())
        .and(checkDateFormat());
  }

  private Predicate<Collection<ShopUnit>> checkTypeMatchRowsWithSameId() {
    return shopUnits -> {
      var ids = shopUnits.stream().map(ShopUnit::getId).toList();
      var types = shopUnits.stream().map(ShopUnit::getType).toList();
      return Objects.equals(repository.countByIdIn(ids),
          repository.countByIdInAndTypeIn(ids, types));
    };
  }

  private Predicate<Collection<ShopUnit>> checkParentType() {
    return shopUnits -> {
      var ids = shopUnits.stream().map(ShopUnit::getId).toList();
      return shopUnits.stream().noneMatch(
          shopUnit -> shopUnit.getChildren().size() > 0
              && shopUnit.getType().equals(ShopUnitType.OFFER))
          && repository.countByIdInAndType(ids, ShopUnitType.OFFER) == 0;
    };
  }

  private Predicate<Collection<ShopUnit>> checkCategoryPrice() {
    return shopUnits -> shopUnits.stream()
        .noneMatch(shopUnit -> shopUnit.getType().equals(ShopUnitType.CATEGORY)
            && Objects.isNull(shopUnit.getPrice()));
  }

  private Predicate<Collection<ShopUnit>> checkOfferPrice() {
    return shopUnits -> shopUnits.stream()
        .noneMatch(shopUnit -> shopUnit.getType().equals(ShopUnitType.OFFER)
            && !Objects.isNull(shopUnit.getPrice())
            && shopUnit.getPrice() >= 0);
  }

  private Predicate<Collection<ShopUnit>> checkIdsUnique() {
    return shopUnits -> shopUnits.stream().distinct().count() <= 1;
  }

  private Predicate<Collection<ShopUnit>> checkDateFormat() {
    return shopUnits -> shopUnits.stream()
        .noneMatch(shopUnit -> {
          try {
            Instant.from(DateTimeFormatter.ISO_INSTANT.parse(shopUnit.getDate()));
            return false;
          } catch (DateTimeParseException e) {
            LOGGER.info("Incorrect date in ShopUnitImportRequest");
            return true;
          }
        });
  }
}
