package com.yandex.enrollment.api.service;

import com.yandex.enrollment.api.model.shop.ShopUnit;
import com.yandex.enrollment.api.model.shop.ShopUnitImportRequest;
import com.yandex.enrollment.api.model.shop.ShopUnitType;
import java.util.ArrayList;
import java.util.Arrays;
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
    Map<String, ShopUnit> shopUnits = new HashMap<>(request.getItems()
        .stream()
        .map(r -> modelMapper.map(r, ShopUnit.class))
        .collect(Collectors.toMap(ShopUnit::getId, shopUnit -> shopUnit)));

    shopUnits.values().forEach(shopUnit -> {
      shopUnit.setDate(request.getUpdateDate());
      String parentId = shopUnit.getParentId();
      if (parentId != null && shopUnits.containsKey(parentId) &&
          shopUnits.get(parentId).getType() == ShopUnitType.CATEGORY) {
        if(shopUnits.get(parentId).getChildren() == null){
          shopUnits.get(parentId).setChildren(new ArrayList<>(List.of(shopUnit)));
        }
        else{
          shopUnits.get(parentId).addChild(shopUnit);
        }
      }
      if(shopUnit.getType() == ShopUnitType.OFFER){
        shopUnit.setUnitsCount(1L);
        shopUnit.setTruePrice(shopUnit.getPrice());
        shopUnit.setChildren(null);
      }
    });
    return shopUnits.values();
  }
}
