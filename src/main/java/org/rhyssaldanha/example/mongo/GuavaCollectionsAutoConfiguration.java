package org.rhyssaldanha.example.mongo;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bson.conversions.Bson;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ResolvableType;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@AutoConfiguration
public class GuavaCollectionsAutoConfiguration {

    @Bean
    public MappingMongoConverter mappingMongoConverter(final MongoMappingContext context,
                                                       final MongoCustomConversions conversions) {
        final MappingMongoConverter mappingConverter = new GuavaCollectionsMappingMongoConverter(NoOpDbRefResolver.INSTANCE, context);
        mappingConverter.setCustomConversions(conversions);
        return mappingConverter;
    }

    private static class GuavaCollectionsMappingMongoConverter extends MappingMongoConverter {

        public GuavaCollectionsMappingMongoConverter(final DbRefResolver dbRefResolver,
                                                     final MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext) {
            super(dbRefResolver, mappingContext);
        }

        @Override
        protected Object readCollectionOrArray(final ConversionContext context,
                                               final Collection<?> source,
                                               final TypeInformation<?> targetType) {
            if (ImmutableCollection.class.isAssignableFrom(targetType.getType())) {
                final var immutableCollectionType = targetType.getType();

                final var componentType = targetType.getComponentType() != null
                        ? targetType.getComponentType()
                        : TypeInformation.OBJECT;
                final var rawComponentType = componentType.getType();

                final var mutableCollectionType = toMutableCollection(immutableCollectionType);
                final var mutableCollection = (Collection<?>) super.readCollectionOrArray(context, source, TypeInformation.of(ResolvableType.forClassWithGenerics(mutableCollectionType, rawComponentType)));

                return toImmutableCollection(mutableCollection, immutableCollectionType);
            }
            return super.readCollectionOrArray(context, source, targetType);
        }

        private static <E> Class<? extends Collection> toMutableCollection(final Class<?> collectionType) {
            if (ImmutableList.class == collectionType) {
                return List.class;
            } else if (ImmutableSet.class == collectionType) {
                return Set.class;
            } else if (ImmutableCollection.class == collectionType) {
                return Collection.class;
            } else {
                throw new IllegalArgumentException("Unsupported ImmutableCollection type: " + collectionType.getName());
            }
        }

        private static <E> ImmutableCollection<?> toImmutableCollection(final Collection<?> mutableCollection, final Class<?> collectionType) {
            Assert.notNull(collectionType, "Collection type must not be null");
            if (ImmutableCollection.class == collectionType || ImmutableList.class == collectionType) {
                return ImmutableList.copyOf(mutableCollection);
            } else if (ImmutableSet.class == collectionType) {
                return ImmutableSet.copyOf(mutableCollection);
            } else {
                throw new IllegalArgumentException("Unsupported ImmutableCollection type: " + collectionType.getName());
            }
        }

        @Override
        protected Map<Object, Object> readMap(final ConversionContext context, final Bson bson, final TypeInformation<?> targetType) {
            if (ImmutableMap.class.isAssignableFrom(targetType.getType())) {
                final var mapType = getTypeMapper().readType(bson, targetType).getType();

                final var keyType = targetType.getComponentType();
                final var valueType = targetType.getMapValueType() != null
                        ? targetType.getRequiredMapValueType()
                        : TypeInformation.OBJECT;

                final var rawKeyType = keyType != null ? keyType.getType() : Object.class;
                final var rawValueType = valueType.getType();

                final var mutableMap = (Map<?, ?>) super.readMap(context, bson, TypeInformation.of(ResolvableType.forClassWithGenerics(Map.class, rawKeyType, rawValueType)));

                return ImmutableMap.copyOf(mutableMap);
            }
            return super.readMap(context, bson, targetType);
        }
    }
}
