package org.rhyssaldanha.example.mongo;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.Builder;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Document(collection = "guava-document")
public record GuavaDocument(
        @Id
        ObjectId id,
        ImmutableCollection<Nested> collection,
        ImmutableList<Nested> list,
        ImmutableSet<Nested> set,
        ImmutableMap<String, Nested> map
) {

    public record Nested(String string, Integer number) {
    }
}
