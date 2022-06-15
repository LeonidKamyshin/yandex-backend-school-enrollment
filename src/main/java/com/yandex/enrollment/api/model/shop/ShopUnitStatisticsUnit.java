package com.yandex.enrollment.api.model.shop;

import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class ShopUnitStatisticsUnit {
  @Id
  @NotNull
  private String id;

  @NotNull
  private String name;

  private String parentId;

  private ShopUnitType type;

  private Long price;

  @NotNull
  private String date;
}
