package org.example.core.auth.service;

import org.example.core.auth.model.SessionUser;
import org.example.core.auth.port.SessionStore;
import org.springframework.session.MapSession;
import org.springframework.session.SessionRepository;

import java.time.Duration;

public class SpringSessionStore implements SessionStore {

    private static final String USER_ATTRIBUTE = "sessionUser";

    private final SessionRepository<MapSession> sessionRepository;
    private final Duration defaultTtl;

    public SpringSessionStore(SessionRepository<MapSession> sessionRepository, Duration defaultTtl) {
        this.sessionRepository = sessionRepository;
        this.defaultTtl = defaultTtl;
    }

    @Override
    public String create(SessionUser user, Duration ttl) {
        MapSession session = sessionRepository.createSession();
        session.setAttribute(USER_ATTRIBUTE, user);
        session.setMaxInactiveInterval(ttl == null ? defaultTtl : ttl);
        sessionRepository.save(session);
        return session.getId();
    }

    @Override
    public SessionUser find(String sessionId) {
        MapSession session = sessionRepository.findById(sessionId);
        return session == null ? null : session.getAttribute(USER_ATTRIBUTE);
    }

    @Override
    public void save(String sessionId, SessionUser user, Duration ttl) {
        MapSession session = sessionRepository.findById(sessionId);
        if (session == null) {
            session = new MapSession(sessionId);
        }
        session.setAttribute(USER_ATTRIBUTE, user);
        session.setMaxInactiveInterval(ttl == null ? defaultTtl : ttl);
        sessionRepository.save(session);
    }

    @Override
    public void remove(String sessionId) {
        sessionRepository.deleteById(sessionId);
    }
}
