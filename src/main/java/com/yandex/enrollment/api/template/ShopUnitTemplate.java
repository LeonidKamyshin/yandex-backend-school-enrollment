package com.yandex.enrollment.api.template;

import com.yandex.enrollment.api.model.shop.ShopUnit;
import com.yandex.enrollment.api.repository.ShopUnitRepository;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
public class ShopUnitTemplate {

  private final MongoTemplate template;
  private final ShopUnitRepository repository;

  @Autowired
  public ShopUnitTemplate(MongoTemplate template,
      ShopUnitRepository repository) {
    this.template = template;
    this.repository = repository;
  }

  public void bulkUpdate(Collection<@NotNull ShopUnit> shopUnits) {
    if (shopUnits.size() == 0) {
      return;
    }

    BulkOperations bulkOps = template.bulkOps(BulkMode.UNORDERED, ShopUnit.class);

    shopUnits.forEach(shopUnit -> {
      Query query = new Query().addCriteria(Criteria.where("id").is(shopUnit.getId()));
      Update update = new Update()
          .addToSet("children").each(shopUnit.getChildren().stream().map(ShopUnit::getId).toArray())
          .set("name", shopUnit.getName())
          .set("parentId", shopUnit.getParentId())
          .set("price", shopUnit.getPrice())
          .set("date", shopUnit.getDate());
      bulkOps.updateOne(query, update);
    });

    bulkOps.execute();
    bulkUpdateChild(shopUnits);
  }

  private void bulkPushChild(Collection<@NotNull ShopUnit> shopUnits) {
    if (shopUnits.size() == 0) {
      return;
    }

    BulkOperations bulkOps = template.bulkOps(BulkMode.UNORDERED, ShopUnit.class);

    shopUnits.forEach(shopUnit -> {
      Query query = new Query().addCriteria(Criteria.where("id").is(shopUnit.getId()));
      Update update = new Update()
          .push("children").each(shopUnit.getChildren().stream().map(ShopUnit::getId).toArray())
          .set("date", shopUnit.getDate());
      bulkOps.updateOne(query, update);
    });

    bulkOps.execute();
  }

  private void bulkPullChild(Collection<@NotNull ShopUnit> shopUnits) {
    if (shopUnits.size() == 0) {
      return;
    }

    BulkOperations bulkOps = template.bulkOps(BulkMode.UNORDERED, ShopUnit.class);

    shopUnits.forEach(shopUnit -> {
      Query query = new Query().addCriteria(Criteria.where("id").is(shopUnit.getId()));
      Update update = new Update()
          .pullAll("children", shopUnit.getChildren().stream().map(ShopUnit::getId).toArray())
          .set("date", shopUnit.getDate());
      bulkOps.updateOne(query, update);
    });

    bulkOps.execute();
  }

  private void bulkUpdateChild(Collection<@NotNull ShopUnit> shopUnits) {
    if (shopUnits.size() == 0) {
      return;
    }

    Map<String, ShopUnit> shopUnitsById =
        shopUnits.stream().collect(Collectors.toMap(ShopUnit::getId, shopUnit -> shopUnit));
    HashMap<String, ShopUnit> pullChildRequest = new HashMap<>();
    HashMap<String, ShopUnit> pushChildRequest = new HashMap<>();
    Collection<ShopUnit> repositoryShopUnits =
        repository.getAllParentIdByIdIn(shopUnits.stream().map(ShopUnit::getId).toList());

    repositoryShopUnits.forEach(repositoryShopUnit -> {
      ShopUnit updateShopUnit = shopUnitsById.get(repositoryShopUnit.getId());
      if (!Objects.equals(updateShopUnit.getParentId(), repositoryShopUnit.getParentId())) {
        pullChildRequest.putIfAbsent(repositoryShopUnit.getParentId(), new ShopUnit());
        ShopUnit pullShopUnit = new ShopUnit();
        pullShopUnit.setId(repositoryShopUnit.getId());
        pullShopUnit.setDate(repositoryShopUnit.getDate());
        pullChildRequest.get(repositoryShopUnit.getParentId())
            .addChild(pullShopUnit);

        pushChildRequest.putIfAbsent(updateShopUnit.getParentId(), new ShopUnit());
        ShopUnit pushShopUnit = new ShopUnit();
        pullShopUnit.setId(updateShopUnit.getId());
        pullShopUnit.setDate(updateShopUnit.getDate());
        pushChildRequest.get(updateShopUnit.getParentId())
            .addChild(pushShopUnit);
      }
    });

    bulkPullChild(pullChildRequest.values());
    bulkPushChild(pushChildRequest.values());
  }
}
