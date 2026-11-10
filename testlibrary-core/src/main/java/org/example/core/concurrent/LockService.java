package org.example.core.concurrent;

public interface LockService {

    LockHandle acquire();

    void release(LockHandle handle);
}
