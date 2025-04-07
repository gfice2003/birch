package tech.grove.birch.serialization;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.Maps;
import tech.grove.birch.patterns.builder.GenericBuilder;
import tech.grove.birch.serialization.accessors.InstanceAccessor;
import tech.grove.birch.serialization.accessors.JsonAccessor;
import tech.grove.birch.serialization.accessors.NodeAccessor;
import tech.grove.birch.serialization.creators.AccessorFactory;
import tech.grove.birch.serialization.creators.MapperBuilder;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class FluentMapper {

    private static final class Default {
        private static final Module[]               MODULES      = {new JavaTimeModule()};
        private static final Mappers                MAPPERS      = new Mappers(null);
        private static final AccessorFactory        FACTORY      = new AccessorFactory(MAPPERS);
        private static final Consumer<ObjectMapper> CONFIGURATOR = mapper -> {
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.configOverride(BigDecimal.class).setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.STRING));
        };
    }

    public static JsonAccessor json(String json) {
        return Default.FACTORY.newJson(json);
    }

    public static <N extends JsonNode> NodeAccessor<N> node(N node) {
        return Default.FACTORY.newNode(node);
    }

    public static <I> InstanceAccessor<I> instance(I instance) {
        return Default.FACTORY.newInstance(instance);
    }

    public static class MapperStarter extends GenericBuilder<MapperStarter> {

        private final Mappers         mappers = new Mappers(null);
        private final AccessorFactory factory = new AccessorFactory(mappers);

        public ObjectMapperSetter withMapper(MapperMode mode) {
            return mapper -> setAndReturnThis(mapper, m -> mappers.register(mode, m), NullValueMode.THROW);
        }

        public JsonAccessor json(String json) {
            return factory.newJson(json);
        }

        public <N extends JsonNode> NodeAccessor<N> node(N node) {
            return factory.newNode(node);
        }

        public <I> InstanceAccessor<I> instance(I instance) {
            return factory.newInstance(instance);
        }
    }

    public interface ObjectMapperSetter {
        MapperStarter set(ObjectMapper mapper);
    }

    private record Mappers(Map<MapperMode, ObjectMapper> mappers) implements MapperResolver {

        public Mappers {
            mappers = Optional.ofNullable(mappers).orElseGet(Maps::newConcurrentMap);
        }

        public void register(MapperMode mode, ObjectMapper mapper) {
            if (mapper != null) {
                mappers.put(mode, mapper);
            }
        }

        @Override
        public ObjectMapper resolveFor(MapperMode mode) {
            return mappers.computeIfAbsent(mode, x -> MapperBuilder.mapperFor(x)
                    .configured(Default.CONFIGURATOR)
                    .withModules(Default.MODULES)
                    .build());
        }
    }
}
