package com.yandex.enrollment.api.template;

import com.yandex.enrollment.api.controller.ShopUnitController;
import com.yandex.enrollment.api.model.shop.ShopUnit;
import com.yandex.enrollment.api.model.shop.ShopUnitStatisticsUnit;
import com.yandex.enrollment.api.model.shop.ShopUnitType;
import com.yandex.enrollment.api.repository.ShopUnitRepository;
import com.yandex.enrollment.api.repository.ShopUnitStatisticUnitRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

/**
 * Шаблон для выполнения запросов в коллекции {@link ShopUnit}
 */
@Component
public class ShopUnitTemplate {

  private static final Logger LOGGER = LogManager.getLogger(ShopUnitController.class);

  private final MongoTemplate template;
  private final ShopUnitRepository shopUnitRepository;
  private final ShopUnitStatisticUnitRepository shopUnitStatisticUnitRepository;

  @Autowired
  public ShopUnitTemplate(MongoTemplate template,
      ShopUnitRepository shopUnitRepository,
      ShopUnitStatisticUnitRepository shopUnitStatisticUnitRepository
  ) {
    this.template = template;
    this.shopUnitRepository = shopUnitRepository;
    this.shopUnitStatisticUnitRepository = shopUnitStatisticUnitRepository;
  }

  /**
   * Вставляет документы, обновляет если уже существуют
   *
   * @param shopUnits Документы которые надо вставить
   */
  public void bulkUpsert(Collection<@NotNull ShopUnit> shopUnits) {
    if (shopUnits.size() == 0) {
      return;
    }

    List<String> ids = shopUnits.stream().map(ShopUnit::getId).toList();
    HashSet<String> updateIds = shopUnitRepository.findExistingIds(ids).stream()
        .map(ShopUnit::getId).collect(Collectors.toCollection(HashSet::new));
    List<ShopUnit> insertShopUnits = shopUnits.stream()
        .filter(shopUnit -> !updateIds.contains(shopUnit.getId())).toList();

    List<ShopUnit> updateShopUnits = shopUnits.stream()
        .filter(shopUnit -> updateIds.contains(shopUnit.getId())).toList();

    Collection<ShopUnit> oldShopUnits = shopUnitRepository
        .findWithoutChildrenAllByIdIn(updateShopUnits.stream().map(ShopUnit::getId).toList());

    bulkInsert(insertShopUnits, shopUnits);
    bulkUpdate(updateShopUnits, oldShopUnits);

    String date = shopUnits.stream().findFirst().orElse(ShopUnit.builder().build()).getDate();
    LOGGER.info("Копирую по дате: " + date);
    saveStatisticOnDate(date);
  }

  /**
   * Вставляет элементы, обновляет цену предков, обновляет дату
   *
   * @param insertShopUnits Элементы для вставки
   */
  private void bulkInsert(Collection<@NotNull ShopUnit> insertShopUnits,
      Collection<@NotNull ShopUnit> upsertShopUnits) {
    if (insertShopUnits.size() == 0) {
      return;
    }

    Map<String, ShopUnit> shopUnitsById =
        upsertShopUnits.stream().collect(Collectors.toMap(ShopUnit::getId, shopUnit -> shopUnit));

    shopUnitRepository.insert(insertShopUnits);
    insertShopUnits.stream().filter(shopUnit -> shopUnit.getType() == ShopUnitType.OFFER)
        .forEach(shopUnit -> incPrice(shopUnit.getParentId(), shopUnit, true, true));

    BulkOperations bulkOps = template.bulkOps(BulkMode.UNORDERED, ShopUnit.class);
    insertShopUnits.stream().filter(shopUnit -> shopUnit.getParentId() != null)
        .forEach(shopUnit -> {
          Query query = new Query().addCriteria(Criteria.where("id").is(shopUnit.getParentId()));
          Update update = new Update()
              .push("children", shopUnit.getId())
              .set("date", shopUnit.getDate());
          bulkOps.updateOne(query, update);
        });
    try {
      bulkOps.execute();
    } catch (IllegalArgumentException ignored) {
    }

    insertShopUnits.forEach(shopUnit -> {
      if (!shopUnitsById.containsKey(shopUnit.getParentId())) {
        pushChild(shopUnit, shopUnit.getParentId());
      }
    });
  }

  private void bulkUpdate(Collection<@NotNull ShopUnit> shopUnits,
      Collection<@NotNull ShopUnit> oldShopUnits) {
    if (shopUnits.size() == 0) {
      return;
    }

    BulkOperations bulkOps = template.bulkOps(BulkMode.UNORDERED, ShopUnit.class);

    shopUnits.forEach(shopUnit -> {
      Query query = new Query().addCriteria(Criteria.where("id").is(shopUnit.getId()));
      Update update = new Update()
          .set("name", shopUnit.getName());
      bulkOps.updateOne(query, update);
    });

    try {
      bulkOps.execute();
    } catch (IllegalArgumentException ignored) {
    }

    Map<String, ShopUnit> oldShopUnitsById =
        oldShopUnits.stream().collect(Collectors.toMap(ShopUnit::getId, shopUnit -> shopUnit));

    shopUnits.stream().filter(shopUnit -> shopUnit.getType() == ShopUnitType.OFFER)
        .forEach(shopUnit -> {
          ShopUnit curShopUnit = ShopUnit.builder().id(shopUnit.getId())
              .date(shopUnit.getDate())
              .truePrice(
                  shopUnit.getTruePrice() - oldShopUnitsById.get(shopUnit.getId()).getTruePrice())
              .build();
          incPrice(curShopUnit.getId(), curShopUnit, false, true);
        });

    shopUnits.forEach(shopUnit -> moveChild(shopUnit,
        oldShopUnitsById.get(shopUnit.getId()).getParentId(), shopUnit.getParentId()));
  }

  private void moveChild(ShopUnit child, String prevParentId, String newParentId) {
    if (Objects.equals(prevParentId, newParentId)) {
      return;
    }
    if (child.getType() == ShopUnitType.CATEGORY) {
      ShopUnit repositoryChild = shopUnitRepository.findWithoutChildrenIdById(child.getId());
      child.setTruePrice(repositoryChild.getTruePrice());
      child.setUnitsCount(repositoryChild.getUnitsCount());
    }
    LOGGER.info("Пушу ребёнка: " + child);
    LOGGER.info("Был родитель: " + prevParentId);
    LOGGER.info("Стал родитель: " + newParentId);
    subPrice(prevParentId, child, true, true);
    pullChild(child, prevParentId);

    pushChild(child, newParentId);
    incPrice(newParentId, child, true, true);
  }

  /**
   * Массово добавляет детей (Не обязательно одной вершине). Сами обьекты не добавляются,
   * добавляется только связь родитель <-> сын Дети добавляются родителям с id = parentId.
   * Поддерживает цену, дату.
   */
  private void pushChild(ShopUnit child, @NotNull String parentId) {
    BulkOperations bulkOps = template.bulkOps(BulkMode.UNORDERED, ShopUnit.class);
    LOGGER.info("!!! Дата для " + child.getId() + " " + child.getDate());
    LOGGER.info("Ставлю ребёнка: " + child);
    LOGGER.info("Родитель ребёнка: " + parentId);
    Query query = new Query().addCriteria(Criteria.where("id").is(parentId));
    Update update = new Update()
        .addToSet("children", child.getId())
        .set("date", child.getDate());
    bulkOps.updateOne(query, update);

    Query queryChild = new Query().addCriteria(Criteria.where("id").is(child.getId()));
    Update updateChild = new Update()
        .set("parentId", child.getParentId())
        .set("date", child.getDate());
    bulkOps.updateOne(queryChild, updateChild);

    try {
      bulkOps.execute();
    } catch (IllegalArgumentException ignored) {
    }
  }

  /**
   * Делает массовое удаление детей (Не обязательно одной вершины) Сами обьекты не удаляются,
   * удаляется только связь родитель <-> сын Дети удаляются у родителей с id = parentId.
   * Поддерживает цену, дату.
   */
  private void pullChild(ShopUnit child, @NotNull String parentId) {
    BulkOperations bulkOps = template.bulkOps(BulkMode.UNORDERED, ShopUnit.class);

    LOGGER.info("!!! Дата для " + child.getId() + " " + child.getDate());
    Query query = new Query().addCriteria(Criteria.where("id").is(parentId));
    Update update = new Update()
        .pull("children", child.getId())
        .set("date", child.getDate());
    bulkOps.updateOne(query, update);
    try {
      bulkOps.execute();
    } catch (IllegalArgumentException ignored) {
    }
  }

  /**
   * Прибавляет цену юнита и всех его предков Используется чтобы обновить цену при
   * добавлении/обновлении вершины из дерева, чтобы корректно поддерживать Значение цены, число
   * элементов в поддереве и дату обновления
   *
   * @param changeUnitsCount Вычитать ли число офферов, которые были у вычитаемого юнита
   * @param updateDate       Обновлять ли дату
   */
  @SuppressWarnings("SameParameterValue")
  private void incPrice(String id, ShopUnit value, boolean changeUnitsCount, boolean updateDate) {
    if (Objects.isNull(value.getTruePrice())) {
      return;
    }

    LOGGER.info("Начинаю увеличивать цену по value: " + value);
    LOGGER.info("Начиная с Id: " + id);
    BulkOperations bulkOps = template.bulkOps(BulkMode.UNORDERED, ShopUnit.class);
    ShopUnit curShopUnit;
    while (id != null) {
      curShopUnit = shopUnitRepository.findWithoutChildrenIdById(id);
      id = curShopUnit.getParentId();
      LOGGER.info("Текущий id: " + curShopUnit.getId());
      Query query = new Query().addCriteria(Criteria.where("id").is(curShopUnit.getId()));
      Update update = new Update();
      if (updateDate) {
        update.set("date", value.getDate());
      }

      LOGGER.info("Увеличиваю truePrice на: " + value.getTruePrice());
      update.inc("truePrice", value.getTruePrice());
      curShopUnit.setTruePrice(curShopUnit.getTruePrice() + value.getTruePrice());

      LOGGER.info("Поставил локально truePrice на: " + curShopUnit.getTruePrice());

      if (changeUnitsCount) {
        update.inc("unitsCount", value.getUnitsCount());
        curShopUnit.setUnitsCount(curShopUnit.getUnitsCount() + value.getUnitsCount());
      }
      if (curShopUnit.getUnitsCount() == 0) {
        update.set("price", null);
      } else {
        update.set("price", curShopUnit.getTruePrice() / curShopUnit.getUnitsCount());
      }
      bulkOps.updateOne(query, update);
    }
    try {
      bulkOps.execute();
    } catch (IllegalArgumentException ignored) {
    }
  }

  /**
   * Вычитает цену из юнита и всех его предков Используется чтобы обновить цену при
   * удалении/обновлении вершины из дерева, чтобы корректно поддерживать Значение цены, число
   * элементов в поддереве и дату обновления
   *
   * @param changeUnitsCount Вычитать ли число офферов, которые были у вычитаемого юнита
   * @param updateDate       оОновлять ли дату
   */
  @SuppressWarnings("SameParameterValue")
  private void subPrice(String id, ShopUnit value, boolean changeUnitsCount, boolean updateDate) {
    if (Objects.isNull(value.getTruePrice())) {
      return;
    }

    BulkOperations bulkOps = template.bulkOps(BulkMode.UNORDERED, ShopUnit.class);
    ShopUnit curShopUnit;
    while (id != null) {
      curShopUnit = shopUnitRepository.findWithoutChildrenIdById(id);
      id = curShopUnit.getParentId();

      Query query = new Query().addCriteria(Criteria.where("id").is(curShopUnit.getId()));
      Update update = new Update();
      if (updateDate) {
        update.set("date", value.getDate());
      }

      update.inc("truePrice", -value.getTruePrice());
      curShopUnit.setTruePrice(curShopUnit.getTruePrice() - value.getTruePrice());

      if (changeUnitsCount) {
        update.inc("unitsCount", -value.getUnitsCount());
        curShopUnit.setUnitsCount(curShopUnit.getUnitsCount() - value.getUnitsCount());
      }

      if (curShopUnit.getUnitsCount() == 0) {
        update.set("price", null);
      } else {
        update.set("price", curShopUnit.getTruePrice() / curShopUnit.getUnitsCount());
      }
      bulkOps.updateOne(query, update);

    }
    try {
      bulkOps.execute();
    } catch (IllegalArgumentException ignored) {
    }
  }

  /**
   * Удаляет элемент и всех его детей, обновляет price, дату обновления не меняет.
   *
   * @param shopUnit Элемент, который нужно удалить
   */
  public void deleteShopUnit(ShopUnit shopUnit) {
    subPrice(shopUnit.getParentId(), shopUnit, true, false);
    List<String> ids = getSubtreeIds(shopUnit);
    shopUnitRepository.deleteAllById(ids);
    shopUnitStatisticUnitRepository.deleteAllByIdIn(ids);
  }

  /**
   * @param root Стартовая вершина
   * @return Возвращает id всех потомков включая себя
   */
  private List<String> getSubtreeIds(ShopUnit root) {
    List<String> ids = new ArrayList<>();
    Stack<ShopUnit> q = new Stack<>();
    q.push(root);
    while (!q.empty()) {
      ShopUnit cur = q.peek();
      q.pop();
      ids.add(cur.getId());
      cur.getChildren().forEach(q::push);
    }
    return ids;
  }

  private void saveStatisticOnDate(String date) {
    Collection<ShopUnitStatisticsUnit> statisticsUnits = shopUnitRepository
        .findAllWithoutChildrenByDate(date);
    LOGGER.info("Получил статистики: " + statisticsUnits);
    shopUnitStatisticUnitRepository.insert(statisticsUnits);
  }
}
