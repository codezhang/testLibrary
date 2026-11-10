package org.example.shell.auth;

import org.example.core.auth.model.SessionUser;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class ShellUserSession {

    private final AtomicReference<SessionUser> current = new AtomicReference<>();

    public SessionUser currentOrDefault() {
        SessionUser user = current.get();
        return user != null ? user : SessionUser.defaultAdmin();
    }

    public SessionUser current() {
        return current.get();
    }

    public void login(SessionUser user) {
        current.set(user);
    }

    public void logout() {
        current.set(null);
    }
}
