package com.yandex.enrollment.api.model.result;

import com.yandex.enrollment.api.model.error.Error;

/**
 * Result, который выдает конвертер
 *
 * @param <T> Класс объекта
 */
public class ConversationResult<T> extends Result<T> {

  public ConversationResult(T result) {
    super(result);
  }

  public ConversationResult(Error error) {
    super(error);
  }
}
