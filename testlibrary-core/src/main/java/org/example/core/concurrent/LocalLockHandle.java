package org.example.core.concurrent;

import java.util.concurrent.locks.ReentrantLock;

final class LocalLockHandle implements LockHandle {

    private final ReentrantLock delegate;
    private boolean released;

    LocalLockHandle(ReentrantLock delegate) {
        this.delegate = delegate;
    }

    @Override
    public synchronized void release() {
        if (!released) {
            released = true;
            delegate.unlock();
        }
    }
}
