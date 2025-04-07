package tech.grove.birch.threading;

import tech.grove.birch.common.Scope;
import tech.grove.birch.patterns.builder.GenericBuilder;

import java.util.function.Supplier;

public class ThreadLocalScope<T> extends GenericBuilder<ThreadLocalScope<T>> implements Supplier<T>, Scope {

    private final ThreadLocal<T> value;
    private final Supplier<T>    defaultFactory;

    public ThreadLocalScope() {
        this(null);
    }

    public ThreadLocalScope(Supplier<T> defaultFactory) {
        this.defaultFactory = defaultFactory;
        this.value          = new ThreadLocal<>();
    }

    public ThreadLocalScope<T> initialize(T value) {
        return setAndReturnThis(value, this.value::set);
    }

    @Override
    public T get() {
        var result = getRaw();

        if (result == null && defaultFactory != null) {
            value.set(result = defaultFactory.get());
        }

        return result;
    }

    public T getRaw() {
        return value.get();
    }

    @Override
    public void close() {

        value.remove();
    }
}