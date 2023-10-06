package org.rhyssaldanha.example.mongo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.Assertions;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.rhyssaldanha.example.mongo.GuavaDocument.Nested;

@DataMongoTest
@Import(GuavaCollectionsAutoConfiguration.class)
class GuavaCollectionsTest {

    @Autowired
    private GuavaRepository guavaRepository;

    @Autowired
    private MongoTemplate template;

    @Test
    void canSave() {
        final var id = new ObjectId();
        final var guavaDocument = getGuavaDocument(id);

        guavaRepository.save(guavaDocument);

        final var guavaDocumentString = template.findById(id, String.class, "guava-document");
        Assertions.assertThat(guavaDocumentString)
                .contains("""
                        "collection": [{"string": "a", "number": 123}]\
                        """)
                .contains("""
                        "list": [{"string": "a", "number": 123}]\
                        """)
                .contains("""
                        "set": [{"string": "a", "number": 123}]\
                        """)
                .contains("""
                        "map": {"key": {"string": "a", "number": 123}}\
                        """);
    }

    @Test
    void canLoad() {
        final var id = new ObjectId();
        final var guavaDocument = getGuavaDocument(id);

        guavaRepository.save(guavaDocument);
        final var maybeSavedGuavaDocument = guavaRepository.findById(id);

        Assertions.assertThat(maybeSavedGuavaDocument).hasValueSatisfying(savedGuavaDocument ->
                Assertions.assertThat(savedGuavaDocument)
                        .usingRecursiveComparison()
                        .isEqualTo(guavaDocument));
    }

    private static GuavaDocument getGuavaDocument(final ObjectId id) {
        return GuavaDocument.builder()
                .id(id)
                .collection(ImmutableList.of(nested()))
                .list(ImmutableList.of(nested()))
                .set(ImmutableSet.of(nested()))
                .map(ImmutableMap.of("key", nested()))
                .build();
    }

    private static Nested nested() {
        return new Nested("a", 123);
    }
}