package com.yandex.enrollment.api.repository;

import com.yandex.enrollment.api.model.shop.ShopUnitStatisticsUnit;
import java.util.Collection;
import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

/**
 * Репозиторий для работы с {@link ShopUnitStatisticsUnit}
 */
public interface ShopUnitStatisticUnitRepository extends
    MongoRepository<ShopUnitStatisticsUnit, String> {

  /**
   * Находит статистику документа с заданным id и date в указанном интервале
   *
   * @param id        Заданный id
   * @param startDate Начало интервала для date
   * @param endDate   Конец интервала для date
   * @return Найденные документы
   */
  @Query(value = "{'id': {$eq: ?0},'date': {$gte: ?1, $lte: ?2}}")
  Collection<ShopUnitStatisticsUnit> findByIdAndDateInterval(String id, String startDate,
      String endDate);

  /**
   * Удаляет все документы с заданными id
   *
   * @param ids Заданные id
   */
  @DeleteQuery(value = "{'id': {$in: ?0}}")
  void deleteAllByIdIn(Collection<String> ids);
}
