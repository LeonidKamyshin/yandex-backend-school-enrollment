package com.yandex.enrollment.api.controller;

import com.yandex.enrollment.api.exception.ApiException;
import com.yandex.enrollment.api.model.error.Error;
import com.yandex.enrollment.api.model.result.ApiResult;
import com.yandex.enrollment.api.model.shop.ShopUnit;
import com.yandex.enrollment.api.model.shop.ShopUnitImportRequest;
import com.yandex.enrollment.api.service.ShopUnitService;
import java.util.Optional;
import javax.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShopUnitController {

  private static final Logger LOGGER = LogManager.getLogger(ShopUnitController.class);

  private final ShopUnitService service;

  @Autowired
  public ShopUnitController(ShopUnitService service) {
    this.service = service;
  }

  @PostMapping(value = "/imports", consumes = {"application/json"})
  public void importShopUnit(@Valid @RequestBody ShopUnitImportRequest request)
      throws ApiException {
    LOGGER.info("Entering api endpoint to import shop units: " + request.getItems().toString());
    Optional<Error> result = service.importShopUnit(request);
    if (result.isPresent()) {
      throw new ApiException(result.get());
    }
  }

  @GetMapping(value = "/nodes/{id}")
  public ShopUnit getShopUnitById(@PathVariable("id") String id) throws ApiException {
    LOGGER.info("Entering api endpoint to get shop unit by id: " + id);
    ApiResult<ShopUnit> result = service.getShopUnitById(id);
    if (result.hasErrors()) {
      throw new ApiException(result.getError());
    } else {
      return result.getResult();
    }
  }

  @DeleteMapping(value = "/delete/{id}")
  public void deleteShopUnitById(@PathVariable("id") String id) throws ApiException {
    LOGGER.info("Entering api endpoint to delete shop unit by id: " + id);
    Optional<Error> result = service.deleteShopUnitById(id);
    if (result.isPresent()) {
      throw new ApiException(result.get());
    }
  }

}