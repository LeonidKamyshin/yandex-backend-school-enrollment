package com.yandex.enrollment.api.model.shop;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Объект, описывающий запрос на добавление товаров / категорий
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ShopUnitImportRequest {

  @Valid
  @NotNull
  @NotEmpty
  private List<ShopUnitImport> items;

  @NotNull
  private String updateDate;
}
