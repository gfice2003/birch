package tech.grove.birch.delegates;

public interface ThrowingSupplier<R, E extends Exception> {
    R get() throws E;
}
