package org.example.core.auth.service;

import org.example.core.auth.SessionContext;
import org.example.core.auth.model.SessionUser;
import org.example.core.auth.port.CurrentUserProvider;
import org.example.core.auth.port.SessionService;
import org.example.core.auth.port.SessionStore;

import java.time.Duration;

public class DefaultSessionService implements SessionService {

    private final CurrentUserProvider currentUserProvider;
    private final SessionStore sessionStore;
    private final Duration ttl;

    public DefaultSessionService(CurrentUserProvider currentUserProvider,
                                 SessionStore sessionStore,
                                 Duration ttl) {
        this.currentUserProvider = currentUserProvider;
        this.sessionStore = sessionStore;
        this.ttl = ttl;
    }

    @Override
    public SessionUser currentUser() {
        return currentUserProvider.currentUser();
    }

    @Override
    public String login(SessionUser user) {
        String sessionId = sessionStore.create(user, ttl);
        SessionContext.setSessionId(sessionId);
        return sessionId;
    }

    @Override
    public void logout() {
        String sessionId = SessionContext.getSessionId();
        if (sessionId != null) {
            sessionStore.remove(sessionId);
        }
        SessionContext.clear();
    }
}
