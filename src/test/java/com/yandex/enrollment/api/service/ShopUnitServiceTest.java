package com.yandex.enrollment.api.service;

import static org.junit.jupiter.api.Assertions.*;

import com.yandex.enrollment.api.model.shop.ShopUnit;
import com.yandex.enrollment.api.model.shop.ShopUnitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ShopUnitServiceTest {

  @BeforeEach
  void t(){
    var lol = ShopUnitType.valueOf("OFFER");
    var kek = ShopUnitType.valueOf("1");
    var s = 2;
  }
  @Test
  void importShopUnit() {
  }

  @Test
  void getShopUnitById() {
  }

  @Test
  void deleteShopUnitById() {
  }
}