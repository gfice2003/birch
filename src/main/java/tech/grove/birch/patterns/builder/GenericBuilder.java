package tech.grove.birch.patterns.builder;

import java.util.function.Consumer;

import static tech.grove.birch.reflection.Reflector.cast;

public abstract class GenericBuilder<B extends GenericBuilder<B>> {

    protected B runAndReturnThis(Runnable action) {

        if (action != null) {
            action.run();
        }

        return cast(this);
    }

    protected <T> B setAndReturnThis(T value, Consumer<T> setter) {
        return setAndReturnThis(value, setter, NullValueMode.SKIP);
    }

    protected <T> B setAndReturnThis(T value, Consumer<T> setter, NullValueMode nullMode) {

        if (setter != null) {

            var set = true;

            if (value == null) {
                switch (nullMode) {
                    case THROW -> throw new IllegalArgumentException("value");
                    case SKIP -> set = false;
                }
            }

            if (set) {
                setter.accept(value);
            }
        }

        return cast(this);
    }

    protected enum NullValueMode {
        ACCEPT,
        THROW,
        SKIP
    }
}
