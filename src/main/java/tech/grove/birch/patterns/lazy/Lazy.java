package tech.grove.birch.patterns.lazy;

import java.util.Optional;
import java.util.function.Supplier;

public class Lazy<T> implements Supplier<T> {

    private volatile T           instance;
    private final    Supplier<T> factory;
    private final    Object      syncRoot;

    public Lazy(Supplier<T> factory) {

        Optional.ofNullable(factory).orElseThrow(() -> new IllegalArgumentException("factory"));

        this.factory  = factory;
        this.instance = null;
        this.syncRoot = new Object();
    }

    //<editor-fold desc="Unit test seams">

    T getInstance() {
        return instance;
    }

    Supplier<T> getFactory() {
        return factory;
    }

    Object getSyncRoot() {
        return syncRoot;
    }

    //</editor-fold>

    @Override
    public T get() {

        if (instance == null) {
            synchronized (syncRoot) {
                if (instance == null) {
                    Optional.ofNullable(instance = factory.get())
                            .orElseThrow(() -> new RuntimeException("Invalid factory instance. Factory should never produce NULL!"));
                }
            }
        }

        return instance;
    }

    public boolean isInitialized() {
        return (instance != null);
    }

    public void reset() {
        instance = null;
    }
}