package com.yandex.enrollment.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import com.yandex.enrollment.api.model.error.ErrorType;
import com.yandex.enrollment.api.model.result.ValidationResult;
import com.yandex.enrollment.api.model.shop.ShopUnit;
import com.yandex.enrollment.api.model.shop.ShopUnitImport;
import com.yandex.enrollment.api.model.shop.ShopUnitImportRequest;
import com.yandex.enrollment.api.model.shop.ShopUnitType;
import com.yandex.enrollment.api.repository.ShopUnitRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.mock.mockito.MockBean;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShopUnitValidationServiceTest {

  private static final String UPDATE_DATE = "2022-06-28T21:12:01.000Z";
  private static final String UPDATE_DATE_BAD_FORMAT = "28-2022T21:12:01.000Z";
  private static final String REPOSITORY_OFFER_ID = "REPOSITORY_OFFER_ID";

  private ShopUnitValidationService validationService;

  @Mock
  private ShopUnitRepository repository;

  @BeforeEach
  void initService() {
    ShopUnitConverterService converterService = new ShopUnitConverterService();
    when(repository.countByIdInAndTypeIn(any(), any())).thenReturn(1L);
    when(repository.getAllWithoutChildrenIdByIdIn(any())).thenReturn(
        List.of(ShopUnit.builder().id(REPOSITORY_OFFER_ID).type(ShopUnitType.OFFER).build()));
    validationService = new ShopUnitValidationService(repository, converterService);
  }

  @ParameterizedTest
  @MethodSource("createTestFail")
  public void validateShopUnitImportRequestFail(ShopUnitImportRequest request) {
    ValidationResult<Collection<ShopUnit>> result =
        validationService.validateShopUnitImportRequest(request);
    assertThat(result.hasErrors()).isTrue();
    assertThat(result.getError()).isEqualTo(ErrorType.VALIDATION_FAILED_ERROR.getError());
  }

  private static Stream<Arguments> createTestFail() {
    return Stream.of(
        Arguments.of(createRequestSameType()),
        Arguments.of(createRequestSameId()),
        Arguments.of(createRequestParentTypeIsOffer()),
        Arguments.of(createRequestCategoryWithPrice()),
        Arguments.of(createRequestOfferWithNoPrice()),
        Arguments.of(createRequestDateBadFormat())
    );
  }

  private static ShopUnitImportRequest createRequestSameType() {
    List<ShopUnitImport> items = List.of(ShopUnitImport.builder().id(REPOSITORY_OFFER_ID)
        .name("name:3").parentId(null).type(ShopUnitType.CATEGORY).price(null).build()
    );

    return ShopUnitImportRequest.builder().items(items).updateDate(UPDATE_DATE).build();
  }

  private static ShopUnitImportRequest createRequestSameId() {
    List<ShopUnitImport> items = List.of(
        ShopUnitImport.builder().id("1").name("name:3").parentId(null).type(ShopUnitType.OFFER)
            .price(100L).build(),
        ShopUnitImport.builder().id("1").name("name:1").parentId(null).type(ShopUnitType.CATEGORY)
            .price(null).build()
    );

    return ShopUnitImportRequest.builder().items(items).updateDate(UPDATE_DATE).build();
  }

  private static ShopUnitImportRequest createRequestParentTypeIsOffer() {
    List<ShopUnitImport> items = List.of(
        ShopUnitImport.builder().id("1").name("name:3").parentId("2").type(ShopUnitType.OFFER)
            .price(100L).build(),
        ShopUnitImport.builder().id("2").name("name:1").parentId(null).type(ShopUnitType.OFFER)
            .price(100L).build()
    );

    return ShopUnitImportRequest.builder().items(items).updateDate(UPDATE_DATE).build();
  }

  private static ShopUnitImportRequest createRequestCategoryWithPrice() {
    List<ShopUnitImport> items = List.of(
        ShopUnitImport.builder().id("1").name("name:1").parentId(null).type(ShopUnitType.CATEGORY)
            .price(1L).build()
    );

    return ShopUnitImportRequest.builder().items(items).updateDate(UPDATE_DATE).build();
  }

  private static ShopUnitImportRequest createRequestOfferWithNoPrice() {
    List<ShopUnitImport> items = List.of(
        ShopUnitImport.builder().id("1").name("name:3").parentId("2").type(ShopUnitType.OFFER)
            .price(null).build()
    );

    return ShopUnitImportRequest.builder().items(items).updateDate(UPDATE_DATE).build();
  }

  private static ShopUnitImportRequest createRequestDateBadFormat() {
    List<ShopUnitImport> items = List.of(
        ShopUnitImport.builder().id("1").name("name:3").parentId(null).type(ShopUnitType.OFFER)
            .price(100L).build(),
        ShopUnitImport.builder().id("2").name("name:1").parentId(null).type(ShopUnitType.CATEGORY)
            .price(null).build()
    );

    return ShopUnitImportRequest.builder().items(items).updateDate(UPDATE_DATE_BAD_FORMAT).build();
  }

  @ParameterizedTest
  @MethodSource("createTestSuccess")
  public void validateShopUnitImportRequestSuccess(ShopUnitImportRequest request) {
    ValidationResult<Collection<ShopUnit>> result =
        validationService.validateShopUnitImportRequest(request);
    assertThat(result.hasErrors()).isFalse();
  }

  private static Stream<Arguments> createTestSuccess() {
    List<ShopUnitImport> items = new ArrayList<>();
    items.add(ShopUnitImport.builder()
        .id("1").name("name:1").parentId(null).type(ShopUnitType.CATEGORY).price(null).build());
    items.add(ShopUnitImport.builder()
        .id("2").name("name:2").parentId("1").type(ShopUnitType.CATEGORY).price(null).build());
    items.add(ShopUnitImport.builder()
        .id("3").name("name:3").parentId("1").type(ShopUnitType.OFFER).price(100L).build());
    ShopUnitImportRequest request = ShopUnitImportRequest.builder().items(items).updateDate(
        UPDATE_DATE).build();

    return Stream.of(Arguments.of(request));
  }
}