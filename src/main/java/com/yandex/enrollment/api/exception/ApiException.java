package com.yandex.enrollment.api.exception;

import com.yandex.enrollment.api.model.error.Error;
import lombok.Getter;

/**
 * Исключение возникающие во время исполнения api
 */
@Getter
public class ApiException extends Exception {

  private final Error error;

  public ApiException(Error error) {
    super(error.getMessage());
    this.error = error;
  }
}
