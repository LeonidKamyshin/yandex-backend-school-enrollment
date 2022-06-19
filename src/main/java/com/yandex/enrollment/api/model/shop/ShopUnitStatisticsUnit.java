package com.yandex.enrollment.api.model.shop;

import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "statistic")
public class ShopUnitStatisticsUnit {
  @Id
  private String primaryKey;

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
