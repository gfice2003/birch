package tech.grove.birch.serialization.creators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import tech.grove.birch.patterns.lazy.Lazy;
import tech.grove.birch.reflection.Reflector;
import tech.grove.birch.serialization.MapperMode;
import tech.grove.birch.serialization.MapperResolver;
import tech.grove.birch.serialization.accessors.AbstractAccessor;
import tech.grove.birch.serialization.accessors.InstanceAccessor;
import tech.grove.birch.serialization.accessors.JsonAccessor;
import tech.grove.birch.serialization.accessors.NodeAccessor;

import java.util.function.Supplier;

import static tech.grove.birch.reflection.Reflector.cast;

public class AccessorFactory {

    private final MapperResolver mapperResolver;

    private final Lazy<JsonAccessor>        json     = new Lazy<>(this::newJson);
    private final Lazy<NodeAccessor<?>>     node     = new Lazy<>(this::newNode);
    private final Lazy<InstanceAccessor<?>> instance = new Lazy<>(this::newInstance);

    public AccessorFactory(MapperResolver mapperResolver) {
        this.mapperResolver = mapperResolver;
    }

    public ObjectMapper mapper(MapperMode mode) {
        return mapperResolver.resolveFor(mode);
    }

    public JsonAccessor newJson(String json) {
        return getAndInitialize(this.json, json);
    }

    public <N extends JsonNode> NodeAccessor<N> newNode(N node) {
        return getAndInitialize(() -> Reflector.<NodeAccessor<N>>cast(this.node.get()), node);
    }

    public <I> InstanceAccessor<I> newInstance(I instance) {
        return getAndInitialize(() -> Reflector.<InstanceAccessor<I>>cast(this.instance.get()), instance);
    }

    private JsonAccessor newJson() {
        return new JsonAccessor(this);
    }

    private NodeAccessor<?> newNode() {
        return new NodeAccessor<>(this);
    }

    private InstanceAccessor<?> newInstance() {
        return new InstanceAccessor<>(this);
    }

    private <T, A extends AbstractAccessor<T, A>> A getAndInitialize(Supplier<A> accessor, T value) {
        return accessor.get().initialize(value);
    }
}
