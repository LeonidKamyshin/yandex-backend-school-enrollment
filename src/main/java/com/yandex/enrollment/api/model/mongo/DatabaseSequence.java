package com.yandex.enrollment.api.model.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Документ, в котором хранятся счётчики id, используемые для автоинкрементации id
 */
@Data
@Document(collection = "database_sequences")
public class DatabaseSequence {

  @Id
  private String id;

  private long seq = 0;
}
