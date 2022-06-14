package com.yandex.enrollment.api.model;

import java.util.List;
import lombok.Data;

@Data
public class ShopUnitStatisticResponse {
  List<ShopUnitStatisticsUnit> items;
}
