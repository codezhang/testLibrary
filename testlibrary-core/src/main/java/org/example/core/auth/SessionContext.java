package org.example.core.auth;

import org.example.core.auth.model.SessionUser;

public final class SessionContext {

    private static final ThreadLocal<String> SESSION_ID = new ThreadLocal<>();
    private static final ThreadLocal<SessionUser> USER_OVERRIDE = new ThreadLocal<>();

    private SessionContext() {
    }

    public static String getSessionId() {
        return SESSION_ID.get();
    }

    public static void setSessionId(String sessionId) {
        SESSION_ID.set(sessionId);
    }

    public static SessionUser getUser() {
        return USER_OVERRIDE.get();
    }

    public static void setUser(SessionUser user) {
        if (user == null) {
            USER_OVERRIDE.remove();
        } else {
            USER_OVERRIDE.set(user);
        }
    }

    public static void clearUser() {
        USER_OVERRIDE.remove();
    }

    public static void clear() {
        SESSION_ID.remove();
        USER_OVERRIDE.remove();
    }
}
