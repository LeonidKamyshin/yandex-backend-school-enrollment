package com.yandex.enrollment.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShopUnitValidationServiceTest {

  private static final String UPDATE_DATE = "2022-06-28T21:12:01.000Z";
  private static final String UPDATE_DATE_BAD_FORMAT = "28-2022T21:12:01.000Z";
  private static final String REPOSITORY_OFFER_ID = "3fa85f64-5717-4562-b3fc-2c963f66a000";

  private ShopUnitValidationService validationService;

  @Mock
  private ShopUnitRepository repository;

  @BeforeEach
  void initService() {
    when(repository.countByIdIn(any())).thenReturn(0L);
    when(repository.findAllWithoutChildrenIdByIdIn(any())).thenReturn(
        List.of(ShopUnit.builder().id(REPOSITORY_OFFER_ID).type(ShopUnitType.OFFER).build()));
    validationService = new ShopUnitValidationService(repository);
  }

  @ParameterizedTest
  @MethodSource("createTestFail")
  public void validateImportRequestFail(Collection<ShopUnit> request) {
    ValidationResult<Collection<ShopUnit>> result =
        validationService.validateImportRequest(request);
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
        Arguments.of(createRequestDateBadFormat()),
        Arguments.of(createRequestNoParent())
    );
  }

  private static Collection<ShopUnit> createRequestSameType() {
    return List.of(ShopUnit.builder().id(REPOSITORY_OFFER_ID)
        .name("name:3").parentId(null).type(ShopUnitType.CATEGORY).price(null).date(UPDATE_DATE).build()
    );
  }

  private static Collection<ShopUnit> createRequestSameId() {
    return List.of(
        ShopUnit.builder().id("3fa85f64-5717-4562-b3fc-2c963f66a441").name("name:3")
            .parentId(null).type(ShopUnitType.OFFER).price(100L).date(UPDATE_DATE).build(),
        ShopUnit.builder().id("3fa85f64-5717-4562-b3fc-2c963f66a441").name("name:1")
            .parentId(null).type(ShopUnitType.CATEGORY).price(null).date(UPDATE_DATE).build()
    );
  }

  private static Collection<ShopUnit> createRequestParentTypeIsOffer() {
    return List.of(
        ShopUnit.builder().id("3fa85f64-5717-4562-b3fc-2c963f66a441").name("name:3")
            .parentId("3fa85f64-5717-4562-b3fc-2c963f66a442").type(ShopUnitType.OFFER)
            .price(100L)
            .date(UPDATE_DATE).build(),
        ShopUnit.builder().id("3fa85f64-5717-4562-b3fc-2c963f66a442").name("name:1")
            .parentId(null).type(ShopUnitType.OFFER)
            .price(100L)
            .date(UPDATE_DATE).build()
    );
  }

  private static Collection<ShopUnit> createRequestCategoryWithPrice() {
    return List.of(
        ShopUnit.builder().id("3fa85f64-5717-4562-b3fc-2c963f66a441").name("name:1")
            .parentId(null).type(ShopUnitType.CATEGORY).price(1L)
            .date(UPDATE_DATE).build()
    );
  }

  private static Collection<ShopUnit> createRequestOfferWithNoPrice() {
    return List.of(
        ShopUnit.builder().id("3fa85f64-5717-4562-b3fc-2c963f66a441").name("name:3")
            .parentId(null).type(ShopUnitType.OFFER).price(null)
            .date(UPDATE_DATE).build()
    );
  }

  private static Collection<ShopUnit> createRequestDateBadFormat() {
    return List.of(
        ShopUnit.builder().id("3fa85f64-5717-4562-b3fc-2c963f66a441").name("name:3")
            .parentId(null).type(ShopUnitType.OFFER).price(100L)
            .date(UPDATE_DATE).build(),
        ShopUnit.builder().id("3fa85f64-5717-4562-b3fc-2c963f66a442").name("name:1")
            .parentId(null).type(ShopUnitType.CATEGORY).price(null)
            .date(UPDATE_DATE_BAD_FORMAT).build()
    );
  }

  private static Collection<ShopUnit> createRequestNoParent() {
    return List.of(
        ShopUnit.builder().id("3fa85f64-5717-4562-b3fc-2c963f66a441").name("name:3")
            .parentId("3fa85f64-5717-4562-b3fc-2c963f66a442").type(ShopUnitType.OFFER).price(100L)
            .date(UPDATE_DATE)
            .build()
    );
  }

  @ParameterizedTest
  @MethodSource("createTestSuccess")
  public void validateShopUnitImportRequestSuccess(Collection<ShopUnit> request) {
    ValidationResult<Collection<ShopUnit>> result =
        validationService.validateImportRequest(request);
    assertThat(result.hasErrors()).isFalse();
  }

  private static Stream<Arguments> createTestSuccess() {
    List<ShopUnit> items = List.of(ShopUnit.builder()
            .id("3fa85f64-5717-4562-b3fc-2c963f66a441").name("name:1").parentId(null)
            .type(ShopUnitType.CATEGORY).price(null)
            .date(UPDATE_DATE).build(),
        ShopUnit.builder()
            .id("3fa85f64-5717-4562-b3fc-2c963f66a442").name("name:2")
            .parentId("3fa85f64-5717-4562-b3fc-2c963f66a441").type(ShopUnitType.CATEGORY)
            .price(null)
            .date(UPDATE_DATE)
            .build(),
        ShopUnit.builder()
            .id("3fa85f64-5717-4562-b3fc-2c963f66a443").name("name:3")
            .parentId("3fa85f64-5717-4562-b3fc-2c963f66a441").type(ShopUnitType.OFFER).price(100L)
            .date(UPDATE_DATE)
            .build()
    );

    return Stream.of(Arguments.of(items));
  }
}