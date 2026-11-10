package org.example.core.concurrent;

import java.util.concurrent.locks.ReentrantLock;

public class LocalLockService implements LockService {

    private final ReentrantLock lock = new ReentrantLock(true);

    @Override
    public LockHandle acquire() {
        lock.lock();
        return new LocalLockHandle(lock);
    }

    @Override
    public void release(LockHandle handle) {
        if (handle != null) {
            handle.release();
        }
    }
}
