package com.yandex.enrollment.api.events;

import com.yandex.enrollment.api.model.shop.ShopUnitStatisticsUnit;
import com.yandex.enrollment.api.template.DatabaseSequenceTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
public class ShopUnitStatisticsUnitModelListener extends
    AbstractMongoEventListener<ShopUnitStatisticsUnit> {

  private final DatabaseSequenceTemplate template;

  @Autowired
  public ShopUnitStatisticsUnitModelListener(DatabaseSequenceTemplate template) {
    this.template = template;
  }

  @Override
  public void onBeforeConvert(BeforeConvertEvent<ShopUnitStatisticsUnit> event) {
    event.getSource().setDbId(template.increment(ShopUnitStatisticsUnit.SEQUENCE_NAME));
  }
}