package tech.grove.birch.serialization.creators;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.google.common.collect.Maps;
import tech.grove.birch.patterns.builder.GenericBuilder;
import tech.grove.birch.serialization.MapperMode;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MapperBuilder extends GenericBuilder<MapperBuilder> {

    private static final Map<MapperMode, Supplier<ObjectMapper>> FACTORIES = Maps.newHashMap();

    static {
        FACTORIES.put(MapperMode.JSON, ObjectMapper::new);
        FACTORIES.put(MapperMode.YAML, () -> new ObjectMapper(new YAMLFactory().enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)));
    }

    public static MapperBuilder mapperFor(MapperMode mode) {
        return new MapperBuilder(mode);
    }

    private final ObjectMapper mapper;

    private MapperBuilder(MapperMode mode) {
        this.mapper = Optional.ofNullable(mode)
                .map(FACTORIES::get)
                .map(Supplier::get)
                .orElseThrow(() -> new IllegalArgumentException("Cannot create mapper for mode: " + mode));
    }

    public MapperBuilder configured(Consumer<ObjectMapper> configure) {
        return setAndReturnThis(configure, x -> x.accept(mapper), NullValueMode.SKIP);
    }

    public MapperBuilder withIntrospector(JacksonAnnotationIntrospector introspector) {
        return setAndReturnThis(introspector, mapper::setAnnotationIntrospector, NullValueMode.SKIP);
    }

    public MapperBuilder withModules(Module... modules) {
        return setAndReturnThis(modules, module -> Arrays.stream(module)
                .forEach(mapper::registerModule), NullValueMode.SKIP);
    }

    public ObjectMapper build() {
        return mapper;
    }
}
