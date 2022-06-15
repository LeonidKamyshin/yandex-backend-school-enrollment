package com.yandex.enrollment.api.model.error;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class Error {
  @NotNull
  private int code;

  @NotNull
  private String message;

  public Error(int code, String message){
    this.code = code;
    this.message = message;
  }
}
