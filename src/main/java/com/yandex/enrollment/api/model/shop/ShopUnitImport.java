package com.yandex.enrollment.api.model.shop;

import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
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
