package com.yandex.enrollment.api.repository;

import com.yandex.enrollment.api.model.shop.ShopUnitStatisticsUnit;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ShopUnitStatisticUnitRepository extends
    MongoRepository<ShopUnitStatisticsUnit, String> {
}
