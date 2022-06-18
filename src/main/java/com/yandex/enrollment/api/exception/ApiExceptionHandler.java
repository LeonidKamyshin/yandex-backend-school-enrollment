package com.yandex.enrollment.api.exception;

import com.yandex.enrollment.api.model.error.Error;
import com.yandex.enrollment.api.model.error.ErrorType;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException.BadRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Log4j2
@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(value = { HttpMessageNotReadableException.class, MethodArgumentNotValidException.class })
  protected ResponseEntity<Object> handleMethodArgumentNotValid(Exception ex) {
    log.info("Кидаю 400 ошибку");
    return ResponseEntity.status(400).body(ErrorType.VALIDATION_FAILED_ERROR.getError());
  }

  @ExceptionHandler(ApiException.class)
  protected ResponseEntity<Object> handleApiException(ApiException ex) {
    Error error = ex.getError();
    return ResponseEntity.status(error.getCode()).body(error);
  }
}
