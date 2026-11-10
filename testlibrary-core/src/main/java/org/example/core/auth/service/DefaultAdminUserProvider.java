package org.example.core.auth.service;

import org.example.core.auth.SessionContext;
import org.example.core.auth.model.SessionUser;
import org.example.core.auth.port.CurrentUserProvider;
import org.example.core.auth.port.SessionStore;

public class DefaultAdminUserProvider implements CurrentUserProvider {

    private final SessionStore sessionStore;
    private final boolean defaultAdminEnabled;

    public DefaultAdminUserProvider(SessionStore sessionStore) {
        this(sessionStore, true);
    }

    public DefaultAdminUserProvider(SessionStore sessionStore, boolean defaultAdminEnabled) {
        this.sessionStore = sessionStore;
        this.defaultAdminEnabled = defaultAdminEnabled;
    }

    @Override
    public SessionUser currentUser() {
        SessionUser override = SessionContext.getUser();
        if (override != null) {
            return override;
        }
        String sessionId = SessionContext.getSessionId();
        if (sessionId == null) {
            return defaultAdminEnabled ? SessionUser.defaultAdmin() : null;
        }
        return sessionStore.find(sessionId);
    }
}
