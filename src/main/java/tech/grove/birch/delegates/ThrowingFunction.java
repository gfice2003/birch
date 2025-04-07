package tech.grove.birch.delegates;

public interface ThrowingFunction<T, R, E extends Exception> {
    R apply(T data) throws E;
}
