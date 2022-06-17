package com.yandex.enrollment.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.yandex.enrollment.api.model.shop.ShopUnit;
import com.yandex.enrollment.api.model.shop.ShopUnitImport;
import com.yandex.enrollment.api.model.shop.ShopUnitImportRequest;
import com.yandex.enrollment.api.model.shop.ShopUnitType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ShopUnitConverterServiceTest {

  private static final String UPDATE_DATE = "2022-06-28T21:12:01.000Z";

  ShopUnitConverterService converterService;

  @BeforeEach
  void initService() {
    converterService = new ShopUnitConverterService();
  }


  @ParameterizedTest
  @MethodSource("createTestWithChildren")
  public void convertShopUnitImportRequestWithChildren(ShopUnitImportRequest request,
      List<ShopUnit> expected) {
    Collection<ShopUnit> result = converterService.convertShopUnitImportRequest(request);
    assertThat(result).isEqualTo(expected);
  }

  private static Stream<Arguments> createTestWithChildren() {
    List<ShopUnitImport> items = new ArrayList<>();
    items.add(ShopUnitImport.builder()
        .id("1").name("name:1").parentId(null).type(ShopUnitType.CATEGORY).price(null).build());
    items.add(ShopUnitImport.builder()
        .id("2").name("name:2").parentId("1").type(ShopUnitType.CATEGORY).price(null).build());
    items.add(ShopUnitImport.builder()
        .id("3").name("name:3").parentId("1").type(ShopUnitType.OFFER).price(100L).build());
    ShopUnitImportRequest request = ShopUnitImportRequest.builder().items(items).updateDate(
        UPDATE_DATE).build();

    List<ShopUnit> expected = new ArrayList<>();
    expected.add(ShopUnit.builder().id("1").name("name:1").parentId(null)
        .type(ShopUnitType.CATEGORY).price(null).date(UPDATE_DATE).build());
    expected.add(ShopUnit.builder().id("2").name("name:2")
        .parentId("1").type(ShopUnitType.CATEGORY).price(null).date(UPDATE_DATE).build());
    expected.add(ShopUnit.builder().id("3").name("name:3").parentId("1")
        .type(ShopUnitType.OFFER).price(100L).truePrice(100L).unitsCount(1L).date(UPDATE_DATE)
        .build());
    expected.get(0).setChildren(expected.subList(1, 3));

    return Stream.of(Arguments.of(request, expected));
  }

  @ParameterizedTest
  @MethodSource("createTestWithoutChildren")
  public void convertShopUnitImportRequestWithoutChildren(ShopUnitImportRequest request,
      List<ShopUnit> expected) {
    Collection<ShopUnit> result = converterService.convertShopUnitImportRequest(request);
    assertThat(result).isEqualTo(expected);
  }

  private static Stream<Arguments> createTestWithoutChildren() {
    List<ShopUnitImport> items = new ArrayList<>();
    items.add(ShopUnitImport.builder()
        .id("1").name("name:1").parentId(null).type(ShopUnitType.CATEGORY).price(null).build());
    items.add(ShopUnitImport.builder()
        .id("2").name("name:2").parentId(null).type(ShopUnitType.CATEGORY).price(null).build());
    items.add(ShopUnitImport.builder()
        .id("3").name("name:3").parentId(null).type(ShopUnitType.OFFER).price(100L).build());
    ShopUnitImportRequest request = ShopUnitImportRequest.builder().items(items).updateDate(
        UPDATE_DATE).build();

    List<ShopUnit> expected = new ArrayList<>();
    expected.add(ShopUnit.builder().id("1").name("name:1").parentId(null)
        .type(ShopUnitType.CATEGORY).price(null).date(UPDATE_DATE).build());
    expected.add(ShopUnit.builder().id("2").name("name:2")
        .parentId(null).type(ShopUnitType.CATEGORY).price(null).date(UPDATE_DATE).build());
    expected.add(ShopUnit.builder().id("3").name("name:3").parentId(null)
        .type(ShopUnitType.OFFER).price(100L).truePrice(100L).unitsCount(1L).date(UPDATE_DATE)
        .build());

    return Stream.of(Arguments.of(request, expected));
  }
}