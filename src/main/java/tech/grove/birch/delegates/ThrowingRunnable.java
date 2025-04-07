package tech.grove.birch.delegates;

public interface ThrowingRunnable<E extends Exception> {
    void run() throws E;
}
