package com.yandex.enrollment.api.service;

import com.yandex.enrollment.api.model.ShopUnit;
import com.yandex.enrollment.api.model.ShopUnitImportRequest;
import com.yandex.enrollment.api.model.ValidationResult;
import com.yandex.enrollment.api.repository.ShopUnitRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShopUnitService {

  private final ShopUnitRepository repository;
  private final ShopUnitValidationService validationService;

  @Autowired
  public ShopUnitService(ShopUnitRepository repository,
      ShopUnitValidationService validationService) {
    this.repository = repository;
    this.validationService = validationService;
  }

  public void importShopUnit(ShopUnitImportRequest request) {
    ValidationResult<List<ShopUnit>> validationResult =
        validationService.validateShopUnitImportRequest(request);
    if (validationResult.isSuccessful()) {
      repository.insert(validationResult.getValidationResult());
    }
  }

  public Optional<ShopUnit> getShopUnitById(String id){
    return repository.findById(id);
  }

  public void deleteShopUnitById(String id){
    repository.deleteById(id);
  }
}
