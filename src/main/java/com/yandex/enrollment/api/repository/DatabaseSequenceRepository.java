package com.yandex.enrollment.api.repository;

import com.yandex.enrollment.api.model.mongo.DatabaseSequence;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DatabaseSequenceRepository extends MongoRepository<DatabaseSequence, String> {

}
