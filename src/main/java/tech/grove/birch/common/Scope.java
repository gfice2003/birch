package tech.grove.birch.common;

import java.io.Closeable;

public interface Scope extends Closeable {

    void close();
}