package com.yandex.enrollment.api.model.result;

import com.yandex.enrollment.api.model.error.Error;

/**
 * Result, который выдает сервис после валидации
 *
 * @param <T> Класс объекта
 */
public class ValidationResult<T> extends Result<T> {

  public ValidationResult(T result) {
    super(result);
  }

  public ValidationResult(Error error) {
    super(error);
  }
}
