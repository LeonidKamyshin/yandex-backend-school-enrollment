package com.yandex.enrollment.api.repository;

import com.yandex.enrollment.api.model.shop.ShopUnit;
import com.yandex.enrollment.api.model.shop.ShopUnitType;
import java.util.Collection;
import javax.validation.constraints.NotNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ShopUnitRepository extends MongoRepository<ShopUnit, String> {

  // Может работать некорректно т.к. TypeIn чекает наличие в коллекции
  Long countByIdInAndTypeIn(Collection<@NotNull String> id, Collection<@NotNull ShopUnitType> type);

  Long countByIdIn(Collection<@NotNull String> id);

  Long countByIdInAndType(Collection<@NotNull String> id, @NotNull ShopUnitType type);

  @Query(value = "{'_id': {$in: ?0}}", fields = "{'_id': 1}")
  Collection<ShopUnit> getExistingIds(Collection<@NotNull String> id);

  @Query(value = "{'_id': {$in: ?0}}", fields = "{'_id': 1, 'parentId': 1}")
  Collection<ShopUnit> getAllParentIdByIdIn(Collection<@NotNull String> id);
}
