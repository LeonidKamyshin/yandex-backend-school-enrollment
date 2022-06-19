package com.yandex.enrollment.api.repository;

import com.yandex.enrollment.api.model.shop.ShopUnitStatisticsUnit;
import java.util.Collection;
import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ShopUnitStatisticUnitRepository extends
    MongoRepository<ShopUnitStatisticsUnit, String> {

  @Query(value = "{'id': {$eq: ?0},'date': {$gte: ?1, $lte: ?2}}")
  Collection<ShopUnitStatisticsUnit> findByTypeAndDateInterval(String id, String startDate,
      String endDate);

  @DeleteQuery(value = "{'id': {$in: ?0}}")
  void deleteAllByIdIn(Collection<String> ids);
}
