package com.yandex.enrollment.api.model.shop;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NoArgsConstructor;

/**
 * Тип - товар / категория
 */

@Schema(type="string", description = "Тип элемента - категория или товар",
    implementation = ShopUnitType.class)
public enum ShopUnitType {
  OFFER,
  CATEGORY
}
