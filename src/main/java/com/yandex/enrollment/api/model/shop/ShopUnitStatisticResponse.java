package com.yandex.enrollment.api.model.shop;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopUnitStatisticResponse {
  private List<ShopUnitStatisticsUnit> items;
}
