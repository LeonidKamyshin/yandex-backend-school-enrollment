package com.yandex.enrollment.api.model;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection="shop_units")
public class ShopUnit {

  @Id
  String id;
  @NotBlank
  String name;
  @NotBlank
  String date;
  String parentId;
  @NotNull
  ShopUnitType type;
  Long price;
  List<ShopUnit> children;
}
