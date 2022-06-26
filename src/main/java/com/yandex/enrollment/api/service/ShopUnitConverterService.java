package com.yandex.enrollment.api.service;

import com.yandex.enrollment.api.controller.ShopUnitController;
import com.yandex.enrollment.api.model.shop.ShopUnit;
import com.yandex.enrollment.api.model.shop.ShopUnitImportRequest;
import com.yandex.enrollment.api.model.shop.ShopUnitType;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;


/**
 * Сервис для конвертации запросов из внешних во внутренние
 */
@Service
public class ShopUnitConverterService {

  private static final Logger LOGGER = LogManager.getLogger(ShopUnitController.class);

  private final ModelMapper modelMapper = new ModelMapper();

  /**
   * Конвертирует {@link ShopUnitImportRequest} в объекты {@link ShopUnit}
   *
   * @param request Запрос на импорт
   * @return Список {@link ShopUnit} на импорт
   */
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
        shopUnitsById.get(parentId).addChild(shopUnit);
      }
      if (shopUnit.getType() == ShopUnitType.OFFER) {
        shopUnit.setUnitsCount(1L);
        shopUnit.setTruePrice(shopUnit.getPrice());
      }
    });

    LOGGER.info("Сконвертировал запрос");
    return shopUnits;
  }
}
