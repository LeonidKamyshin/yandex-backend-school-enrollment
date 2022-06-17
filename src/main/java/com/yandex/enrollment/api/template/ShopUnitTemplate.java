package com.yandex.enrollment.api.template;

import com.yandex.enrollment.api.controller.ShopUnitController;
import com.yandex.enrollment.api.model.shop.ShopUnit;
import com.yandex.enrollment.api.repository.ShopUnitRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

@Component
public class ShopUnitTemplate {

  private static final Logger LOGGER = LogManager.getLogger(ShopUnitController.class);

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

    Map<String, ShopUnit> repositoryPrice = repository.getAllWithoutChildrenIdByIdIn(
            shopUnits.stream()
                .map(ShopUnit::getId).toList()).stream()
        .collect(Collectors.toMap(ShopUnit::getId, x -> x));

    List<ShopUnit> priceDif = shopUnits.stream().map(shopUnit -> ShopUnit.builder()
        .date(shopUnit.getDate())
        .price(shopUnit.getPrice() - repositoryPrice.get(shopUnit.getId()).getPrice())
        .parentId(shopUnit.getParentId())
        .build()).toList();

    bulkIncPriceWithUpdate(priceDif);
    bulkOps.execute();
    bulkUpdateChild(shopUnits);
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
        repository.getAllWithoutChildrenIdByIdIn(shopUnits.stream().map(ShopUnit::getId).toList());

    repositoryShopUnits.forEach(repositoryShopUnit -> {
      ShopUnit updateShopUnit = shopUnitsById.get(repositoryShopUnit.getId());
      if (!Objects.equals(updateShopUnit.getParentId(), repositoryShopUnit.getParentId())) {
        pullChildRequest.putIfAbsent(repositoryShopUnit.getParentId(), ShopUnit.builder()
            .id(repositoryShopUnit.getParentId()).children(new ArrayList<>()).build());
        ShopUnit pullShopUnit = ShopUnit.builder()
            .id(repositoryShopUnit.getId())
            .date(repositoryShopUnit.getDate())
            .build();
        pullChildRequest.get(repositoryShopUnit.getParentId())
            .addChild(pullShopUnit);

        pushChildRequest.putIfAbsent(updateShopUnit.getParentId(), ShopUnit.builder()
            .id(updateShopUnit.getParentId()).children(new ArrayList<>()).build());
        ShopUnit pushShopUnit = ShopUnit.builder()
            .id(updateShopUnit.getId())
            .date(updateShopUnit.getDate())
            .build();
        pushChildRequest.get(updateShopUnit.getParentId())
            .addChild(pushShopUnit);
      }
    });

    bulkPullChild(pullChildRequest.values());

    bulkPushChild(pushChildRequest.values());
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

    List<ShopUnit> priceDif = shopUnits.stream().map(shopUnit -> ShopUnit.builder()
        .date(shopUnit.getDate())
        .price(-shopUnit.getPrice())
        .parentId(shopUnit.getParentId())
        .build()).toList();
    bulkIncPriceWithUpdate(priceDif);

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

  public void bulkIncPriceWithInsert(Collection<@NotNull ShopUnit> shopUnits) {
    bulkIncPrice(shopUnits, true);
  }

  public void bulkIncPriceWithUpdate(Collection<@NotNull ShopUnit> shopUnits) {
    bulkIncPrice(shopUnits, false);
  }

  private void bulkIncPrice(Collection<@NotNull ShopUnit> shopUnits, boolean insert) {
    if (shopUnits.size() == 0) {
      return;
    }

    shopUnits.forEach(shopUnit -> {
      if (!Objects.isNull(shopUnit.getPrice())) {
        BulkOperations bulkOps = template.bulkOps(BulkMode.UNORDERED, ShopUnit.class);
        ShopUnit curShopUnit = shopUnit;
        while (!Objects.isNull(curShopUnit.getParentId())) {
          List<ShopUnit> parent = repository.getAllWithoutChildrenIdByIdIn(
              Collections.singleton(curShopUnit.getParentId())).stream().toList();
          curShopUnit = parent.get(0);
          Query query = new Query().addCriteria(Criteria.where("id").is(curShopUnit.getId()));
          Update update = new Update().set("date", shopUnit.getDate());
          if (Objects.isNull(curShopUnit.getPrice())) {
            update.set("truePrice", shopUnit.getPrice());
          } else {
            update.inc("truePrice", shopUnit.getPrice());
          }

          curShopUnit.setTruePrice(curShopUnit.getTruePrice() + shopUnit.getTruePrice());

          if (insert) {
            update.inc("unitsCount", shopUnit.getUnitsCount());
            curShopUnit.setUnitsCount(curShopUnit.getUnitsCount() + shopUnit.getUnitsCount());
          }
          update.set("price", curShopUnit.getTruePrice() / curShopUnit.getUnitsCount());
          bulkOps.updateOne(query, update);
        }
        try {
          bulkOps.execute();
        } catch (IllegalArgumentException ignored) {
        }
      }
    });
  }

  public void bulkInsert(Collection<@NotNull ShopUnit> shopUnits) {
    if (shopUnits.size() == 0) {
      return;
    }

    Map<String, ShopUnit> shopUnitsById =
        shopUnits.stream().collect(Collectors.toMap(ShopUnit::getId, shopUnit -> shopUnit));

    HashMap<String, ShopUnit> pushChildRequest = new HashMap<>();
    shopUnits.forEach(shopUnit -> {
      if (!shopUnitsById.containsKey(shopUnit.getParentId())) {
        pushChildRequest.putIfAbsent(shopUnit.getParentId(),
            ShopUnit.builder().id(shopUnit.getParentId()).children(new ArrayList<>()).build());
        ShopUnit pushShopUnit = ShopUnit.builder()
            .id(shopUnit.getId())
            .date(shopUnit.getDate())
            .build();
        pushChildRequest.get(shopUnit.getParentId())
            .addChild(pushShopUnit);
      }
    });

    LOGGER.debug("Собираюсь запушить детей: " + pushChildRequest.values());
    bulkPushChild(pushChildRequest.values());

    LOGGER.debug("Вставляю юниты: " + shopUnits);
    repository.insert(shopUnits);
    bulkIncPriceWithInsert(shopUnits);
  }
}
