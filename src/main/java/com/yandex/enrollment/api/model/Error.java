package com.yandex.enrollment.api.model;

import lombok.Data;

@Data
public class Error {
  int code;
  String message;
}
