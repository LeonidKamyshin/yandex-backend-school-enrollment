package com.yandex.enrollment.api.service;

import com.yandex.enrollment.api.controller.ShopUnitController;
import com.yandex.enrollment.api.model.error.ErrorType;
import com.yandex.enrollment.api.model.error.Error;
import com.yandex.enrollment.api.model.result.ApiResult;
import com.yandex.enrollment.api.model.result.ValidationResult;
import com.yandex.enrollment.api.model.shop.ShopUnit;
import com.yandex.enrollment.api.model.shop.ShopUnitImportRequest;
import com.yandex.enrollment.api.repository.ShopUnitRepository;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShopUnitService {
  private static final Logger LOGGER = LogManager.getLogger(ShopUnitController.class);

  private final ShopUnitRepository repository;
  private final ShopUnitValidationService validationService;

  @Autowired
  public ShopUnitService(ShopUnitRepository repository,
      ShopUnitValidationService validationService) {
    this.repository = repository;
    this.validationService = validationService;
  }

  public Optional<Error> importShopUnit(ShopUnitImportRequest request) {
    ValidationResult<List<ShopUnit>> validationResult =
        validationService.validateShopUnitImportRequest(request);
    if (!validationResult.hasErrors()) {
      repository.insert(validationResult.getResult());
    }
    return Optional.ofNullable(validationResult.getError());
  }

  public ApiResult<ShopUnit> getShopUnitById(String id){
    Optional<ShopUnit> result = repository.findById(id);
    return result.map(ApiResult::new)
        .orElseGet(() -> new ApiResult<>(ErrorType.ITEM_NOT_FOUND_ERROR.getError()));
  }

  public Optional<Error> deleteShopUnitById(String id){
    if(repository.findById(id).isPresent()){
      repository.deleteById(id);
      return Optional.empty();
    }
    else{
      return Optional.ofNullable(ErrorType.ITEM_NOT_FOUND_ERROR.getError());
    }
  }
}
