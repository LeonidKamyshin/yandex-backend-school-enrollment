package com.yandex.enrollment.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidationResult<T> {

  private Error error;
  private T validationResult;

  public boolean isSuccessful() {
    return error == null;
  }
}
