package org.example.core.auth.port;

import org.example.core.auth.model.SessionUser;

public interface CurrentUserProvider {

    SessionUser currentUser();
}
