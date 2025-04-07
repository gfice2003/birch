package tech.grove.birch.serialization.accessors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import tech.grove.birch.serialization.creators.AccessorFactory;
import tech.grove.birch.serialization.MapperMode;
import tech.grove.birch.serialization.SerializationApi;

public class JsonAccessor extends AbstractAccessor<String, JsonAccessor> implements SerializationApi.JsonAccessor {

    public JsonAccessor(AccessorFactory factory) {
        super(factory);
    }

    @Override
    public String getPretty() {
        return executeAndRelease(x -> mapper(MapperMode.JSON).readTree(x).toPrettyString());
    }

    @Override
    public <N extends JsonNode> NodeAccessor<N> asNode(Class<N> type) {
        return toNode(x -> type.cast(mapper(MapperMode.JSON).readTree(x)));
    }

    @Override
    public <T> InstanceAccessor<T> asType(Class<T> type) {
        return toInstance(x -> mapper(MapperMode.JSON).readValue(x, type));
    }

    @Override
    public <T> InstanceAccessor<T> asType(TypeReference<T> type) {
        return toInstance(x -> mapper(MapperMode.JSON).readValue(x, type));
    }
}
