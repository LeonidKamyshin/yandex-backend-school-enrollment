package com.yandex.enrollment.api.service;

import com.yandex.enrollment.api.model.ShopUnit;
import com.yandex.enrollment.api.model.ShopUnitImportRequest;
import com.yandex.enrollment.api.model.ValidationResult;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
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
    return new ValidationResult<>(null, shopUnits);
  }
}
