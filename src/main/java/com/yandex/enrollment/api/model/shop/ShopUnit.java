package com.yandex.enrollment.api.model.shop;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yandex.enrollment.api.utils.DateUtils;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Объект, описывающий товар / категорию
 */
@Data
@Document(collection = "shop_units")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@NotNull
public class ShopUnit {

  @Id
  @NotNull
  private String id;

  @NotNull
  private String name;

  @NotNull
  private String date;

  private String parentId;

  @NotNull
  private ShopUnitType type;

  private Long price;

  @JsonIgnore
  @Builder.Default
  private Long truePrice = 0L;

  @JsonIgnore
  @Builder.Default
  private Long unitsCount = 0L;

  @Valid
  @Reference
  @Builder.Default
  private List<ShopUnit> children = new ArrayList<>();

  public void addChild(ShopUnit child) {
    children.add(child);
  }

  public void setDate(String date) {
    this.date = DateUtils.unifyDate(date);
  }
}
