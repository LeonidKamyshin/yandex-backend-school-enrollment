package com.yandex.enrollment.api.model.shop;

import java.util.List;
import lombok.Data;

@Data
public class ShopUnitStatisticResponse {
  private List<ShopUnitStatisticsUnit> items;
}
