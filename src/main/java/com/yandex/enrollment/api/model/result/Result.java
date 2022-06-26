package com.yandex.enrollment.api.model.result;

import com.yandex.enrollment.api.model.error.Error;
import java.util.ArrayList;
import java.util.List;

/**
 * Базовый класс, описывающий объект во время исполнения API result - сам объект errors - ошибки,
 * возникшие во время работы API, связанные с этим объектом
 *
 * @param <T> Класс объекта
 */
public abstract class Result<T> {

  private final T result;
  private final List<Error> errors;

  public Result(T result) {
    this.result = result;
    errors = new ArrayList<>();
  }

  public Result(Error error) {
    this.result = null;
    errors = List.of(error);
  }

  public T getResult() {
    return result;
  }

  public boolean hasErrors() {
    return errors.size() > 0;
  }

  public Error getError() {
    if (errors.size() > 0) {
      return errors.get(0);
    } else {
      return null;
    }
  }

  public void addError(Error error) {
    errors.add(error);
  }
}
