package com.yandex.enrollment.api.service;

import com.yandex.enrollment.api.controller.ShopUnitController;
import com.yandex.enrollment.api.model.error.Error;
import com.yandex.enrollment.api.model.error.ErrorType;
import com.yandex.enrollment.api.model.result.ApiResult;
import com.yandex.enrollment.api.model.result.ValidationResult;
import com.yandex.enrollment.api.model.shop.ShopUnit;
import com.yandex.enrollment.api.model.shop.ShopUnitImportRequest;
import com.yandex.enrollment.api.repository.ShopUnitRepository;
import com.yandex.enrollment.api.template.ShopUnitTemplate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShopUnitService {

  private static final Logger LOGGER = LogManager.getLogger(ShopUnitController.class);

  private final ShopUnitTemplate template;
  private final ShopUnitRepository repository;
  private final ShopUnitValidationService validationService;

  @Autowired
  public ShopUnitService(ShopUnitRepository repository,
      ShopUnitValidationService validationService,
      ShopUnitTemplate template) {
    this.repository = repository;
    this.validationService = validationService;
    this.template = template;
  }

  public Optional<Error> importShopUnit(ShopUnitImportRequest request) {
    ValidationResult<Collection<ShopUnit>> validationResult =
        validationService.validateShopUnitImportRequest(request);
    if (!validationResult.hasErrors()) {
      Collection<ShopUnit> shopUnits = validationResult.getResult();
      List<String> ids = shopUnits.stream().map(ShopUnit::getId).toList();
      HashSet<String> updateIds = repository.getExistingIds(ids).stream()
          .map(ShopUnit::getId).collect(Collectors.toCollection(HashSet::new));
      LOGGER.info("Добыл id на апдейт: " + updateIds);
      List<ShopUnit> insertShopUnits = shopUnits.stream()
          .filter(shopUnit -> !updateIds.contains(shopUnit.getId())).toList();
      repository.insert(insertShopUnits);

      List<ShopUnit> updateShopUnits = shopUnits.stream()
          .filter(shopUnit -> updateIds.contains(shopUnit.getId())).toList();

      template.bulkUpdate(updateShopUnits);
    }

    return Optional.ofNullable(validationResult.getError());
  }

  public ApiResult<ShopUnit> getShopUnitById(String id) {
    Optional<ShopUnit> result = repository.findById(id);

    return result.map(ApiResult::new)
        .orElseGet(() -> new ApiResult<>(ErrorType.ITEM_NOT_FOUND_ERROR.getError()));
  }

  //  @Transactional
  public Optional<Error> deleteShopUnitById(String id) {
    if (repository.findById(id).isPresent()) {
      repository.deleteById(id);
      return Optional.empty();
    } else {
      return Optional.ofNullable(ErrorType.ITEM_NOT_FOUND_ERROR.getError());
    }
  }
}
