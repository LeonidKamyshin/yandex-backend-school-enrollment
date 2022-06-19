package com.yandex.enrollment.api.service;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.yandex.enrollment.api.model.mongo.DatabaseSequence;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class NextSequenceService {

  private final MongoOperations operations;

  public NextSequenceService(MongoOperations operations) {
    this.operations = operations;
  }

  @SuppressWarnings("ConstantConditions")
  public Long getNextSequence(String seqName) {
    DatabaseSequence counter = operations.findAndModify(
        query(where("_id").is(seqName)),
        new Update().inc("seq", 1),
        options().returnNew(true).upsert(true),
        DatabaseSequence.class);
    return counter.getSeq();
  }
}
