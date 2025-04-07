package tech.grove.birch.serialization.accessors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import tech.grove.birch.serialization.creators.AccessorFactory;
import tech.grove.birch.serialization.MapperMode;
import tech.grove.birch.serialization.SerializationApi;

public class NodeAccessor<N extends JsonNode> extends AbstractAccessor<N, NodeAccessor<N>> implements SerializationApi.NodeAccessor<N> {

    public NodeAccessor(AccessorFactory factory) {
        super(factory);
    }

    @Override
    public JsonAccessor asJson() {
        return toJson(x -> mapper(MapperMode.JSON).writeValueAsString(x));
    }

    @Override
    public <T> InstanceAccessor<T> asType(Class<T> type) {
        return toInstance(x -> mapper(MapperMode.JSON).treeToValue(x, type));
    }

    @Override
    public <T> InstanceAccessor<T> asType(TypeReference<T> type) {
        return toInstance(x -> mapper(MapperMode.JSON).treeToValue(x, type));
    }
}
