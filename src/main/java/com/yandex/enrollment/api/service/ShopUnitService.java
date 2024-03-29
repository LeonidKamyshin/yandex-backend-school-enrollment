package com.yandex.enrollment.api.service;

import com.yandex.enrollment.api.controller.ShopUnitController;
import com.yandex.enrollment.api.model.error.Error;
import com.yandex.enrollment.api.model.error.ErrorType;
import com.yandex.enrollment.api.model.result.ApiResult;
import com.yandex.enrollment.api.model.result.ValidationResult;
import com.yandex.enrollment.api.model.shop.ShopUnit;
import com.yandex.enrollment.api.model.shop.ShopUnitImportRequest;
import com.yandex.enrollment.api.model.shop.ShopUnitStatisticResponse;
import com.yandex.enrollment.api.model.shop.ShopUnitStatisticsUnit;
import com.yandex.enrollment.api.model.shop.ShopUnitType;
import com.yandex.enrollment.api.repository.ShopUnitRepository;
import com.yandex.enrollment.api.repository.ShopUnitStatisticUnitRepository;
import com.yandex.enrollment.api.template.ShopUnitTemplate;
import com.yandex.enrollment.api.utils.DateUtils;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Сервис для выполнения запросов API
 */
@Service
public class ShopUnitService {

  private static final Logger LOGGER = LogManager.getLogger(ShopUnitController.class);

  private final ShopUnitTemplate template;
  private final ShopUnitRepository shopUnitRepository;
  private final ShopUnitStatisticUnitRepository shopUnitStatisticUnitRepository;
  private final ShopUnitValidationService validationService;
  private final ShopUnitConverterService converterService;

  @Autowired
  public ShopUnitService(ShopUnitRepository shopUnitRepository,
      ShopUnitStatisticUnitRepository shopUnitStatisticUnitRepository,
      ShopUnitValidationService validationService,
      ShopUnitConverterService converterService,
      ShopUnitTemplate template) {
    this.shopUnitRepository = shopUnitRepository;
    this.shopUnitStatisticUnitRepository = shopUnitStatisticUnitRepository;
    this.validationService = validationService;
    this.converterService = converterService;
    this.template = template;
  }

  /**
   * Получает {@link ShopUnit} по заданному id
   *
   * @param id Заданный id
   * @return {@link ApiResult<ShopUnit>} - результат запроса
   */
  public ApiResult<ShopUnit> getShopUnitById(String id) {
    ValidationResult<String> validationResult = validationService.validateUUID(id);
    if (validationResult.hasErrors()) {
      return new ApiResult<>(validationResult.getError());
    }

    Optional<ShopUnit> result = shopUnitRepository.findById(id);
    result.ifPresent(this::resetNulls);
    return result.map(ApiResult::new)
        .orElseGet(() -> new ApiResult<>(ErrorType.ITEM_NOT_FOUND_ERROR.getError()));
  }

  /**
   * Костыль, потому что не смог исправить null -> [] после бд
   *
   * @param root корень запроса get
   */
  private void resetNulls(ShopUnit root) {
    Stack<ShopUnit> q = new Stack<>();
    q.push(root);
    while (!q.empty()) {
      ShopUnit cur = q.peek();
      q.pop();
      if (cur.getType() == ShopUnitType.OFFER) {
        cur.setChildren(null);
        continue;
      }
      cur.getChildren().forEach(q::push);
    }
  }

  /**
   * Получает список товаров, цена которых была обновлена за последние 24 часа от заданной даты
   *
   * @param date Заданная дата
   * @return {@link ApiResult<ShopUnitStatisticResponse>} - результат запроса
   */
  public ApiResult<ShopUnitStatisticResponse> getSales(String date) {
    ValidationResult<String> validationResult = validationService.validateDateFormat(date);
    if (validationResult.hasErrors()) {
      return new ApiResult<>(validationResult.getError());
    } else {
      String dateStart = DateUtils.minus(validationResult.getResult(), 1, ChronoUnit.DAYS);
      String dateEnd = validationResult.getResult();
      List<ShopUnitStatisticsUnit> shopUnits = (List<ShopUnitStatisticsUnit>) shopUnitRepository
          .findAllByTypeAndDateInterval(ShopUnitType.OFFER, dateStart, dateEnd);
      return new ApiResult<>(ShopUnitStatisticResponse.builder().items(shopUnits).build());
    }
  }

  /**
   * Получает статистику {@link ShopUnit} с заданным id за заданный интервал
   *
   * @param id        Заданный id
   * @param dateStart Начало интервала date
   * @param dateEnd   Конец интверала date
   * @return {@link ApiResult<ShopUnitStatisticResponse>} - полученную статистику
   */
  public ApiResult<ShopUnitStatisticResponse> getStatistic(String id, String dateStart,
      String dateEnd) {
    ValidationResult<String> idValidationResult = validationService.validateUUID(id);
    if (idValidationResult.hasErrors()) {
      return new ApiResult<>(idValidationResult.getError());
    }
    ValidationResult<String> dateStartValidationResult =
        validationService.validateDateFormat(dateStart);
    ValidationResult<String> dateEndValidationResult =
        validationService.validateDateFormat(dateEnd);

    if (dateStartValidationResult.hasErrors()) {
      return new ApiResult<>(dateStartValidationResult.getError());
    } else {
      if (dateEndValidationResult.hasErrors()) {
        return new ApiResult<>(dateEndValidationResult.getError());
      }

      dateStart = Optional.ofNullable(dateStartValidationResult.getResult())
          .orElse(DateUtils.MIN_DATE);
      dateEnd = Optional.ofNullable(dateEndValidationResult.getResult()).orElse(DateUtils.MAX_DATE);

      List<ShopUnitStatisticsUnit> shopUnits =
          (List<ShopUnitStatisticsUnit>) shopUnitStatisticUnitRepository
              .findByIdAndDateInterval(id, dateStart, dateEnd);
      return new ApiResult<>(ShopUnitStatisticResponse.builder().items(shopUnits).build());
    }
  }

  /**
   * Удаляет {@link ShopUnit} по заданному id
   *
   * @param id Заданный id
   * @return Ошибку, если возникла
   */
  //  @Transactional
  public Optional<Error> deleteShopUnitById(String id) {
    ValidationResult<String> idValidationResult = validationService.validateUUID(id);
    if (idValidationResult.hasErrors()) {
      return Optional.of(idValidationResult.getError());
    }
    Optional<ShopUnit> root = shopUnitRepository.findById(id);
    if (root.isPresent()) {
      template.deleteShopUnit(root.get());
      return Optional.empty();
    } else {
      return Optional.ofNullable(ErrorType.ITEM_NOT_FOUND_ERROR.getError());
    }
  }

  /**
   * Импортирует объекты
   *
   * @param request Реквест на импорт
   * @return Ошибку, если возникла
   */
  //  @Transactional
  public Optional<Error> importShopUnit(ShopUnitImportRequest request) {
    Collection<ShopUnit> innerRequest = converterService.convertShopUnitImportRequest(request);

    ValidationResult<Collection<ShopUnit>> validationResult =
        validationService.validateImportRequest(innerRequest);

    if (!validationResult.hasErrors()) {
      Collection<ShopUnit> shopUnits = validationResult.getResult();
      template.bulkUpsert(shopUnits);
    }

    return Optional.ofNullable(validationResult.getError());
  }
}