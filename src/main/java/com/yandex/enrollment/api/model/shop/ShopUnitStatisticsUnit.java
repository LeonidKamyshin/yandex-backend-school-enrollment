package com.yandex.enrollment.api.model.shop;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "statistic")
public class ShopUnitStatisticsUnit {
  @JsonIgnore
  public static final String SEQUENCE_NAME = "statistic_sequence";

  @Id
  @JsonIgnore
  private String dbId;

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
