package com.yandex.enrollment.api.model.shop;

import java.time.LocalDateTime;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShopUnitImportRequest {
  @Valid
  @NotNull
  private List<ShopUnitImport> items;

  @NotNull
  private LocalDateTime updateDate;
}
