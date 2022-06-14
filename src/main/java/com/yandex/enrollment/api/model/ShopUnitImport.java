package com.yandex.enrollment.api.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class ShopUnitImport {
  @Id
  String id;
  String name;
  String parentId;
  ShopUnitType type;
  Long price;
}
