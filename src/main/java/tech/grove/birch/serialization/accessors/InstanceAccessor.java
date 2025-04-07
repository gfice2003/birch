package tech.grove.birch.serialization.accessors;

import com.fasterxml.jackson.databind.JsonNode;
import tech.grove.birch.serialization.creators.AccessorFactory;
import tech.grove.birch.serialization.MapperMode;
import tech.grove.birch.serialization.SerializationApi;

public class InstanceAccessor<T> extends AbstractAccessor<T, InstanceAccessor<T>> implements SerializationApi.InstanceAccessor<T> {

    public InstanceAccessor(AccessorFactory factory) {
        super(factory);
    }

    @Override
    public JsonAccessor asJson() {
        return toJson(x -> mapper(MapperMode.JSON).writeValueAsString(x));
    }

    @Override
    public <N extends JsonNode> NodeAccessor<N> asNode(Class<N> type) {
        return toNode(x -> mapper(MapperMode.JSON).convertValue(x, type));
    }
}
