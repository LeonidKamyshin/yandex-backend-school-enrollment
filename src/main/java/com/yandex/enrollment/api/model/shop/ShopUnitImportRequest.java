package com.yandex.enrollment.api.model.shop;

import java.time.LocalDateTime;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ShopUnitImportRequest {

  @Valid
  @NotNull
  private List<ShopUnitImport> items;

  @NotNull
  private String updateDate;
}
