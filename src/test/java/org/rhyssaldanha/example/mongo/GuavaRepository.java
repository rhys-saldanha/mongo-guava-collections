package org.rhyssaldanha.example.mongo;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuavaRepository extends MongoRepository<GuavaDocument, ObjectId> {
}
