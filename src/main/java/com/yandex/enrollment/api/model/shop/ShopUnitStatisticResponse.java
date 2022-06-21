package com.yandex.enrollment.api.model.shop;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Объект, описывающий ответ API на запрос получения статистики
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopUnitStatisticResponse {
  private List<ShopUnitStatisticsUnit> items;
}
