package com.yandex.enrollment.api.model.shop;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

/**
 * Объект, описывающий запрос на добавление товара / категории
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ShopUnitImport {

  @Id
  @NotNull
  private String id;

  @NotNull
  private String name;

  private String parentId;

  @NotNull
  private ShopUnitType type;

  private Long price;
}
