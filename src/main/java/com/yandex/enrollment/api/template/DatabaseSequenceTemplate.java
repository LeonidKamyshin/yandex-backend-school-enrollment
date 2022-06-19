package com.yandex.enrollment.api.template;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.yandex.enrollment.api.controller.ShopUnitController;
import com.yandex.enrollment.api.model.mongo.DatabaseSequence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSequenceTemplate {

  private static final Logger LOGGER = LogManager.getLogger(ShopUnitController.class);

  private final MongoTemplate template;

  @Autowired
  public DatabaseSequenceTemplate(MongoTemplate template) {
    this.template = template;
  }

  @SuppressWarnings("ConstantConditions")
  public String increment(String seqName) {
    DatabaseSequence counter = template.findAndModify(
        query(where("_id").is(seqName)),
        new Update().inc("seq", 1),
        options().returnNew(true).upsert(true),
        DatabaseSequence.class);
    return Long.toString(counter.getSeq());
  }
}
