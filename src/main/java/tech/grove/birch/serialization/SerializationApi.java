package tech.grove.birch.serialization;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

public interface SerializationApi {

    <T> InstanceAccessor<T> instance(T instance);

    interface InstanceAccessor<T> extends Get<T>, AsJson, AsNode {
    }

    <N extends JsonNode> NodeAccessor<N> node(String json);

    interface NodeAccessor<N extends JsonNode> extends Get<N>, AsJson, AsType {
    }

    JsonAccessor json(String json);

    interface JsonAccessor extends Get<String>, AsType, AsNode {

        String getPretty();
    }

    interface Get<T> {

        T get();
    }

    interface AsType {

        <T> InstanceAccessor<T> asType(Class<T> type);

        <T> InstanceAccessor<T> asType(TypeReference<T> type);
    }

    interface AsNode {

        <N extends JsonNode> NodeAccessor<N> asNode(Class<N> type);
    }

    interface AsJson {

        JsonAccessor asJson();
    }
}
