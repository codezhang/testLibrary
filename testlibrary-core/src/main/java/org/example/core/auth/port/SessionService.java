package org.example.core.auth.port;

import org.example.core.auth.model.SessionUser;

public interface SessionService {

    SessionUser currentUser();

    String login(SessionUser user);

    void logout();
}
