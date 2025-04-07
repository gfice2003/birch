package tech.grove.birch.serialization.accessors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import tech.grove.birch.delegates.ThrowingFunction;
import tech.grove.birch.patterns.builder.GenericBuilder;
import tech.grove.birch.serialization.creators.AccessorFactory;
import tech.grove.birch.serialization.MapperMode;
import tech.grove.birch.serialization.SerializationApi;
import tech.grove.birch.threading.ThreadLocalScope;

import java.util.function.Function;

public abstract class AbstractAccessor<T, A extends AbstractAccessor<T, A>> extends GenericBuilder<A> implements SerializationApi.Get<T> {

    private final ThreadLocalScope<T> data = new ThreadLocalScope<>();
    private final AccessorFactory     factory;

    protected AbstractAccessor(AccessorFactory factory) {
        this.factory = factory;
    }

    protected ObjectMapper mapper(MapperMode mode) {
        return factory.mapper(mode);
    }

    public A initialize(T data) {
        return setAndReturnThis(data, this.data::initialize, NullValueMode.THROW);
    }

    @Override
    public T get() {
        return executeAndRelease(x -> x);
    }

    protected JsonAccessor toJson(ThrowingFunction<T, String, JsonProcessingException> function) {
        return toAccessor(factory::newJson, function);
    }

    protected <N extends JsonNode> NodeAccessor<N> toNode(ThrowingFunction<T, N, JsonProcessingException> function) {
        return toAccessor(factory::newNode, function);
    }

    protected <I> InstanceAccessor<I> toInstance(ThrowingFunction<T, I, JsonProcessingException> function) {
        return toAccessor(factory::newInstance, function);
    }

    private <R, S extends AbstractAccessor<R, S>> S toAccessor(Function<R, S> createAndInitialize,
                                                               ThrowingFunction<T, R, JsonProcessingException> function) {
        return createAndInitialize.apply(executeAndRelease(function));
    }

    protected <R> R executeAndRelease(ThrowingFunction<T, R, JsonProcessingException> function) {
        try (data) {
            return function.apply(data.get());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
