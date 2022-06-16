package com.yandex.enrollment.api.service;

import com.yandex.enrollment.api.controller.ShopUnitController;
import com.yandex.enrollment.api.model.error.Error;
import com.yandex.enrollment.api.model.error.ErrorType;
import com.yandex.enrollment.api.model.result.ApiResult;
import com.yandex.enrollment.api.model.result.ValidationResult;
import com.yandex.enrollment.api.model.shop.ShopUnit;
import com.yandex.enrollment.api.model.shop.ShopUnitImportRequest;
import com.yandex.enrollment.api.model.shop.ShopUnitType;
import com.yandex.enrollment.api.repository.ShopUnitRepository;
import com.yandex.enrollment.api.template.ShopUnitTemplate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShopUnitService {

  private static final Logger LOGGER = LogManager.getLogger(ShopUnitController.class);

  private final ShopUnitTemplate template;
  private final ShopUnitRepository repository;
  private final ShopUnitValidationService validationService;

  @Autowired
  public ShopUnitService(ShopUnitRepository repository,
      ShopUnitValidationService validationService,
      ShopUnitTemplate template) {
    this.repository = repository;
    this.validationService = validationService;
    this.template = template;
  }

//  @Transactional
  public Optional<Error> importShopUnit(ShopUnitImportRequest request) {
    ValidationResult<Collection<ShopUnit>> validationResult =
        validationService.validateShopUnitImportRequest(request);
    if (!validationResult.hasErrors()) {
      Collection<ShopUnit> shopUnits = validationResult.getResult();
      List<String> ids = shopUnits.stream().map(ShopUnit::getId).toList();
      HashSet<String> updateIds = repository.getExistingIds(ids).stream()
          .map(ShopUnit::getId).collect(Collectors.toCollection(HashSet::new));
      List<ShopUnit> insertShopUnits = shopUnits.stream()
          .filter(shopUnit -> !updateIds.contains(shopUnit.getId())).toList();


      List<ShopUnit> updateShopUnits = shopUnits.stream()
          .filter(shopUnit -> updateIds.contains(shopUnit.getId())).toList();

      LOGGER.debug("Добыл id на инсерт: " + insertShopUnits);
      LOGGER.debug("Добыл id на апдейт: " + updateShopUnits);

      template.bulkInsert(insertShopUnits);
      template.bulkUpdate(updateShopUnits);
    }

    return Optional.ofNullable(validationResult.getError());
  }

  public ApiResult<ShopUnit> getShopUnitById(String id) {
    Optional<ShopUnit> result = repository.findById(id);
    result.ifPresent(this::resetNulls);
    return result.map(ApiResult::new)
        .orElseGet(() -> new ApiResult<>(ErrorType.ITEM_NOT_FOUND_ERROR.getError()));
  }

  //  @Transactional
  public Optional<Error> deleteShopUnitById(String id) {
    Optional<ShopUnit> root = repository.findById(id);
    if (root.isPresent()) {
      repository.deleteAllById(getSubtreeIds(root.get()));
      return Optional.empty();
    } else {
      return Optional.ofNullable(ErrorType.ITEM_NOT_FOUND_ERROR.getError());
    }
  }

  private List<String> getSubtreeIds(ShopUnit root){
    List<String> ids = new ArrayList<>();
    Stack<ShopUnit> q = new Stack<>();
    q.push(root);
    while(!q.empty()){
      ShopUnit cur = q.peek();
      q.pop();
      ids.add(cur.getId());
      cur.getChildren().forEach(q::push);
    }
    return ids;
  }

  /**
   * Костыль, потому что не смог исправить null -> [] после бд
   * @param root корень запроса get
   */
  private void resetNulls(ShopUnit root){
    Stack<ShopUnit> q = new Stack<>();
    q.push(root);
    while(!q.empty()){
      ShopUnit cur = q.peek();
      q.pop();
      if(cur.getType() == ShopUnitType.OFFER){
        cur.setChildren(null);
        continue;
      }
      cur.getChildren().forEach(q::push);
    }
  }
}
