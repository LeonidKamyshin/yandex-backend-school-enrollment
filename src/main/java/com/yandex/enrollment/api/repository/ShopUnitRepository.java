package com.yandex.enrollment.api.repository;

import com.yandex.enrollment.api.model.shop.ShopUnit;
import com.yandex.enrollment.api.model.shop.ShopUnitStatisticsUnit;
import com.yandex.enrollment.api.model.shop.ShopUnitType;
import java.util.Collection;
import javax.validation.constraints.NotNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

/**
 * Репозиторий для работы с {@link ShopUnit}
 */
public interface ShopUnitRepository extends MongoRepository<ShopUnit, String> {

  /**
   * Считает все документы с заданными id
   *
   * @param id Заданные id
   * @return Число документов
   */
  Long countByIdIn(Collection<@NotNull String> id);

  /**
   * Считает все документы с заданными id и заданным типом
   *
   * @param id   Заданные id
   * @param type Заданный тип
   * @return Число документов
   */
  Long countByIdInAndType(Collection<@NotNull String> id, @NotNull ShopUnitType type);

  /**
   * Находит все документы с заданными id
   *
   * @param id Заданные id
   * @return Найденные документы
   */
  @Query(value = "{'_id': {$in: ?0}}", fields = "{'_id': 1}")
  Collection<ShopUnit> findExistingIds(Collection<@NotNull String> id);

  /**
   * Находит все документы с заданными id
   *
   * @param id Заданные id
   * @return Найденные документы без поля children
   */
  @Query(value = "{'_id': {$in: ?0}}", fields = "{'children': 0}")
  Collection<ShopUnit> findWithoutChildrenAllByIdIn(Collection<@NotNull String> id);

  /**
   * Находит документ с заданным id
   *
   * @param id Заданный id
   * @return Найденный документ без поля children
   */
  @Query(value = "{'_id': {$eq: ?0}}", fields = "{'children': 0}")
  ShopUnit findWithoutChildrenIdById(String id);

  /**
   * Находит все документы с заданным типом и date в указанном интервале
   *
   * @param type      Заданный тип
   * @param startDate Начало интервала для date
   * @param endDate   Конец интервала для date
   * @return Найденные документы без поля children
   */
  @Query(value = "{'type': {$eq: ?0},'date': {$gte: ?1, $lte: ?2}}", fields = "{'children': 0}")
  Collection<ShopUnitStatisticsUnit> findAllByTypeAndDateInterval(ShopUnitType type,
      String startDate, String endDate);

  /**
   * Находит все документы с заданной date
   *
   * @param date Заданная date
   * @return Найденные документы без поля children
   */
  @Query(value = "{'date': {$eq: ?0}}", fields = "{'children': 0}")
  Collection<ShopUnitStatisticsUnit> findAllWithoutChildrenByDate(String date);
}