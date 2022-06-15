package com.yandex.enrollment.api.model.error;

public enum ErrorType {
  ITEM_NOT_FOUND_ERROR(new Error(404, "Item not found")),
  VALIDATION_FAILED_ERROR(new Error(400, "Validation failed"));

  private final Error error;

  ErrorType(Error error) {
    this.error = error;
  }

  public Error getError() {
    return error;
  }

  public String getMessage() {
    return error.getMessage();
  }

  public int getCode() {
    return error.getCode();
  }
}
