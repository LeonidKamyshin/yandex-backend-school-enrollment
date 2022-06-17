package com.yandex.enrollment.api.service;

import com.yandex.enrollment.api.model.shop.ShopUnit;
import com.yandex.enrollment.api.model.shop.ShopUnitImportRequest;
import com.yandex.enrollment.api.model.shop.ShopUnitType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class ShopUnitConverterService {

  private final ModelMapper modelMapper = new ModelMapper();

  public Collection<ShopUnit> convertShopUnitImportRequest(ShopUnitImportRequest request) {
    List<ShopUnit> shopUnits = request.getItems().stream()
        .map(r -> modelMapper.map(r, ShopUnit.class)).toList();

    Map<String, ShopUnit> shopUnitsById = shopUnits.stream().collect(
        Collectors.toMap(ShopUnit::getId, shopUnit -> shopUnit, (a, b) -> a, HashMap::new)
    );

    shopUnitsById.values().forEach(shopUnit -> {
      shopUnit.setDate(request.getUpdateDate());
      String parentId = shopUnit.getParentId();
      if (parentId != null && shopUnitsById.containsKey(parentId) &&
          shopUnitsById.get(parentId).getType() == ShopUnitType.CATEGORY) {
        if (shopUnitsById.get(parentId).getChildren() == null) {
          shopUnitsById.get(parentId).setChildren(new ArrayList<>(List.of(shopUnit)));
        } else {
          shopUnitsById.get(parentId).addChild(shopUnit);
        }
      }
      if (shopUnit.getType() == ShopUnitType.OFFER) {
        shopUnit.setUnitsCount(1L);
        shopUnit.setTruePrice(shopUnit.getPrice());
        shopUnit.setChildren(null);
      }
    });
    return shopUnits;
  }
}
