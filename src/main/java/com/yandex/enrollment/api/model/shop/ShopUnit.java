package com.yandex.enrollment.api.model.shop;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "shop_units")
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

  @Valid
  @Reference
  private List<ShopUnit> children = new ArrayList<>();

  public void addChild(ShopUnit child) {
    children.add(child);
  }
}
