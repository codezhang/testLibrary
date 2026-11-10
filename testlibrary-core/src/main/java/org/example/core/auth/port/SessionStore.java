package org.example.core.auth.port;

import org.example.core.auth.model.SessionUser;

import java.time.Duration;

public interface SessionStore {

    String create(SessionUser user, Duration ttl);

    SessionUser find(String sessionId);

    void save(String sessionId, SessionUser user, Duration ttl);

    void remove(String sessionId);
}
