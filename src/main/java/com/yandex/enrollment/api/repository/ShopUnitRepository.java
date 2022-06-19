package com.yandex.enrollment.api.repository;

import com.yandex.enrollment.api.model.shop.ShopUnit;
import com.yandex.enrollment.api.model.shop.ShopUnitType;
import java.util.Collection;
import javax.validation.constraints.NotNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ShopUnitRepository extends MongoRepository<ShopUnit, String> {

  Long countByIdIn(Collection<@NotNull String> id);

  Long countByIdInAndType(Collection<@NotNull String> id, @NotNull ShopUnitType type);

  @Query(value = "{'_id': {$in: ?0}}", fields = "{'_id': 1}")
  Collection<ShopUnit> findExistingIds(Collection<@NotNull String> id);

  @Query(value = "{'_id': {$in: ?0}}", fields = "{'children': 0}")
  Collection<ShopUnit> findWithoutChildrenAllByIdIn(Collection<@NotNull String> id);

  @Query(value = "{'_id': {$eq: ?0}}", fields = "{'children': 0}")
  ShopUnit findWithoutChildrenIdById(String id);

  @Query(value = "{'type': {$eq: ?0},'date': {$gte: ?1, $lte: ?2}}")
  Collection<ShopUnit> findAllByTypeAndDateInterval(ShopUnitType type, String startDate, String endDate);
}
