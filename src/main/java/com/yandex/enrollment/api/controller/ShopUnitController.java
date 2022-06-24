package com.yandex.enrollment.api.controller;

import com.yandex.enrollment.api.exception.ApiException;
import com.yandex.enrollment.api.model.error.Error;
import com.yandex.enrollment.api.model.error.ErrorType;
import com.yandex.enrollment.api.model.result.ApiResult;
import com.yandex.enrollment.api.model.shop.ShopUnit;
import com.yandex.enrollment.api.model.shop.ShopUnitImportRequest;
import com.yandex.enrollment.api.model.shop.ShopUnitStatisticResponse;
import com.yandex.enrollment.api.service.ShopUnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import javax.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnJndi;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер API
 */
@RestController
public class ShopUnitController {

  private static final Logger LOGGER = LogManager.getLogger(ShopUnitController.class);

  private final ShopUnitService service;

  @Autowired
  public ShopUnitController(ShopUnitService service) {
    this.service = service;
  }
  @Operation(
      description =
          """
              Получить информацию об элементе по идентификатору. При получении информации о
              категории также предоставляется информация о её дочерних элементах.
              """,
      tags = "GET",

      parameters = @Parameter(name = "id", required = true, description = "Идентификатор элемента",
          example = "3fa85f64-5717-4562-b3fc-2c963f66a333"))
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Информация об элементе.", content = {
          @Content(mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      value = """
                          {
                            "id": "3fa85f64-5717-4562-b3fc-2c963f66a111",
                            "name": "Категория",
                            "type": "CATEGORY",
                            "parentId": null,
                            "date": "2022-05-28T21:12:01.000Z",
                            "price": 6,
                            "children": [
                              {
                                "name": "Оффер 1",
                                "id": "3fa85f64-5717-4562-b3fc-2c963f66a222",
                                "price": 4,
                                "date": "2022-05-28T21:12:01.000Z",
                                "type": "OFFER",
                                "parentId": "3fa85f64-5717-4562-b3fc-2c963f66a111"
                              },
                              {
                                "name": "Подкатегория",
                                "type": "CATEGORY",
                                "id": "3fa85f64-5717-4562-b3fc-2c963f66a333",
                                "date": "2022-05-26T21:12:01.000Z",
                                "parentId": "3fa85f64-5717-4562-b3fc-2c963f66a111",
                                "price": 8,
                                "children": [
                                  {
                                    "name": "Оффер 2",
                                    "id": "3fa85f64-5717-4562-b3fc-2c963f66a444",
                                    "parentId": "3fa85f64-5717-4562-b3fc-2c963f66a333",
                                    "date": "2022-05-26T21:12:01.000Z",
                                    "price": 8,
                                    "type": "OFFER"
                                  }
                                ]
                              }
                            ]
                          }
                          """)
              },
              schema = @Schema(implementation = ShopUnit.class))}),
      @ApiResponse(responseCode = "400",
          description = "Невалидная схема документа или входные данные не верны.",
          content = @Content(mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      value = """
                          {
                            "code": 400,
                            "message": "Validation Failed"
                          }
                          """)
              },
              schema = @Schema(implementation = Error.class))),
      @ApiResponse(responseCode = "404",
          description = "Категория/товар не найден.",
          content = @Content(mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      value = """
                          {
                            "code": 404,
                            "message": "Item not found"
                          }
                          """)
              },
              schema = @Schema(implementation = Error.class)))})
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

  @Operation(
      description =
          """
              Получение списка товаров, цена которых была обновлена за последние 24 часа от времени
              переданном в запросе. Обновление цены не означает её изменение. Обновления
              цен удаленных товаров недоступны. При обновлении цены товара, средняя цена
              категории, которая содержит этот товар, тоже обновляется.
              """,
      tags = "GET",
      parameters = @Parameter(name = "date", required = true,
          description = "Дата и время запроса в формате ISO 8601",
          example = "2022-05-28T21:12:01.000Z"))
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Список товаров, цена которых была обновлена.",
          content = {@Content(mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      value = """
                          {
                            "items": [
                              {
                                "id": "3fa85f64-5717-4562-b3fc-2c963f66a444",
                                "name": "Оффер",
                                "date": "2022-05-28T21:12:01.000Z",
                                "parentId": "3fa85f64-5717-4562-b3fc-2c963f66a333",
                                "price": 234,
                                "type": "OFFER"
                              }
                            ]
                          }
                          """)
              },
              schema = @Schema(implementation = ShopUnitStatisticResponse.class))}),
      @ApiResponse(responseCode = "400",
          description = "Невалидная схема документа или входные данные не верны.",
          content = @Content(mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      value = """
                          {
                            "code": 400,
                            "message": "Validation Failed"
                          }
                          """)
              },
              schema = @Schema(implementation = Error.class)))})
  @GetMapping(value = "/sales")
  public ShopUnitStatisticResponse getSales(@RequestParam(name = "date") String date)
      throws ApiException {
    LOGGER.info("Entering api endpoint to get sales by date: " + date);
    ApiResult<ShopUnitStatisticResponse> result = service.getSales(date);
    if (result.hasErrors()) {
      throw new ApiException(result.getError());
    } else {
      return result.getResult();
    }
  }

  @Operation(
      description =
          """
              Получение статистики (истории обновлений) по цене товара/категории за заданный
              интервал. Статистика по удаленным элементам недоступна.
              """,
      tags = "GET",
      parameters = {@Parameter(name = "id", required = true,
          description = "UUID товара/категории для которой будет отображаться статистика",
          example = "3fa85f64-5717-4562-b3fc-2c963f66a333"),
          @Parameter(name = "dateStart",
              description = "Дата и время начала интервала, для которого считается статистика",
              example = "2022-05-28T21:12:01.000Z"),
          @Parameter(name = "dateEnd",
              description = "Дата и время конца интервала, для которого считается статистика",
              example = "2022-05-28T21:12:01.000Z")
      })
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Статистика по элементу.",
          content = {@Content(mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      value = """
                          {
                            "items": [
                              {
                                "id": "3fa85f64-5717-4562-b3fc-2c963f66a444",
                                "name": "Оффер",
                                "date": "2022-05-28T21:12:01.000Z",
                                "parentId": "3fa85f64-5717-4562-b3fc-2c963f66a333",
                                "price": 234,
                                "type": "OFFER"
                              }
                            ]
                          }
                          """)
              },
              schema = @Schema(implementation = ShopUnitStatisticResponse.class))}),
      @ApiResponse(responseCode = "400",
          description = "Некорректный формат запроса или некорректные даты интервала.",
          content = @Content(mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      value = """
                          {
                            "code": 400,
                            "message": "Validation Failed"
                          }
                          """)
              },
              schema = @Schema(implementation = Error.class))),
      @ApiResponse(responseCode = "404",
          description = "Категория/товар не найден.",
          content = @Content(mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      value = """
                          {
                            "code": 404,
                            "message": "Item not found"
                          }
                          """)
              },
              schema = @Schema(implementation = Error.class)))})
  @GetMapping(value = "/node/{id}/statistic")
  public ShopUnitStatisticResponse getStatistic(@PathVariable("id") String id,
      @RequestParam(name = "dateStart", required = false) String dateStart,
      @RequestParam(name = "dateEnd", required = false) String dateEnd) throws ApiException {
    LOGGER.info("Entering api endpoint to get statistic by id: " + id);
    ApiResult<ShopUnitStatisticResponse> result = service.getStatistic(id, dateStart, dateEnd);
    if (result.hasErrors()) {
      throw new ApiException(result.getError());
    } else {
      return result.getResult();
    }
  }

  @Operation(
      description =
          """
              Импортирует новые товары и/или категории. Товары/категории импортированные повторно
              обновляют текущие. Изменение типа элемента с товара на категорию или с категории на
              товар не допускается. Порядок элементов в запросе является произвольным.
              """,
      tags = "POST",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          content = @Content(mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      value = """
                          {
                            "items": [
                              {
                                "id": "3fa85f64-5717-4562-b3fc-2c963f66a444",
                                "name": "Оффер",
                                "parentId": "3fa85f64-5717-4562-b3fc-2c963f66a333",
                                "price": 234,
                                "type": "OFFER"
                              }
                            ],
                            "updateDate": "2022-05-28T21:12:01.000Z"
                          }
                          """)
              })))
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Вставка или обновление прошли успешно."),
      @ApiResponse(responseCode = "400",
          description = "Невалидная схема документа или входные данные не верны.",
          content = @Content(mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      value = """
                          {
                            "code": 400,
                            "message": "Validation Failed"
                          }
                          """)
              },
              schema = @Schema(implementation = Error.class)))})
  @PostMapping(value = "/imports", consumes = {"application/json"})
  public void importShopUnit(@Valid @RequestBody ShopUnitImportRequest request)
      throws ApiException {
    LOGGER.info("Entering api endpoint to import shop units" + request);
    Optional<Error> result = service.importShopUnit(request);
    if (result.isPresent()) {
      throw new ApiException(result.get());
    }
    LOGGER.info("Добавил ревкест");
  }

  @Operation(
      description =
          """
              Удалить элемент по идентификатору. При удалении категории удаляются все дочерние
              элементы
              """,
      tags = "DELETE",
      parameters = @Parameter(name = "id", required = true,
          description = "Идентификатор",
          example = "3fa85f64-5717-4562-b3fc-2c963f66a333"))
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "Удаление прошло успешно."),
      @ApiResponse(responseCode = "400",
          description = "Невалидная схема документа или входные данные не верны.",
          content = @Content(mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      value = """
                          {
                            "code": 400,
                            "message": "Validation Failed"
                          }
                          """)
              },
              schema = @Schema(implementation = Error.class))),
      @ApiResponse(responseCode = "404",
          description = "Категория/товар не найден",
          content = @Content(mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      value = """
                          {
                            "code": 404,
                            "message": "Item not found"
                          }
                          """)
              },
              schema = @Schema(implementation = Error.class)))})
  @DeleteMapping(value = "/delete/{id}")
  public void deleteShopUnitById(@PathVariable("id") String id) throws ApiException {
    LOGGER.info("Entering api endpoint to delete shop unit by id: " + id);
    Optional<Error> result = service.deleteShopUnitById(id);
    if (result.isPresent()) {
      throw new ApiException(result.get());
    }
  }
}