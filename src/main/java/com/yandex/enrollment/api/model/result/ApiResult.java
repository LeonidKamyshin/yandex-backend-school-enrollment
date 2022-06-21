package com.yandex.enrollment.api.model.result;

import com.yandex.enrollment.api.model.error.Error;

/**
 * Result, который получает контроллер от сервисов
 *
 * @param <T> Класс объекта
 */
public class ApiResult<T> extends Result<T> {

  public ApiResult(T result) {
    super(result);
  }

  public ApiResult(Error error) {
    super(error);
  }
}
