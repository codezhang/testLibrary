package org.example.core.auth.port;

import org.example.core.auth.model.UserAccount;

import java.util.Collection;
import java.util.Optional;

public interface UserAccountStore {

    Optional<UserAccount> findByUsername(String username);

    boolean exists(String username);

    UserAccount create(String userId, String username, String password, Collection<String> roles);
}
