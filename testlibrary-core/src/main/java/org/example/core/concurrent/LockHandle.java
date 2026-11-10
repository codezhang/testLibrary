package org.example.core.concurrent;

public interface LockHandle extends AutoCloseable {

    void release();

    @Override
    default void close() {
        release();
    }
}
