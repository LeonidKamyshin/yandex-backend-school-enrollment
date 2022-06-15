package com.yandex.enrollment.api.service;

import com.yandex.enrollment.api.model.shop.ShopUnit;
import com.yandex.enrollment.api.model.shop.ShopUnitImportRequest;
import com.yandex.enrollment.api.model.result.ValidationResult;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class ShopUnitValidationService {

  private final ModelMapper modelMapper;

  public ShopUnitValidationService() {
    this.modelMapper = new ModelMapper();
  }

  public ValidationResult<List<ShopUnit>> validateShopUnitImportRequest(ShopUnitImportRequest request) {
    List<ShopUnit> shopUnits =
        request.getItems()
            .stream()
            .map(r -> modelMapper.map(r, ShopUnit.class))
            .toList();
    return new ValidationResult<>(shopUnits);
  }
}
