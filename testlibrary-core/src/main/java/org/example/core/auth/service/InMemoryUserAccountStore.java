package org.example.core.auth.service;

import org.example.core.auth.model.UserAccount;
import org.example.core.auth.port.PasswordHasher;
import org.example.core.auth.port.UserAccountStore;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryUserAccountStore implements UserAccountStore {

    private static final String DEFAULT_ADMIN = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    private final ConcurrentMap<String, UserAccount> accounts = new ConcurrentHashMap<>();
    private final PasswordHasher passwordHasher;

    public InMemoryUserAccountStore(PasswordHasher passwordHasher) {
        this.passwordHasher = passwordHasher;
        accounts.put(DEFAULT_ADMIN, new UserAccount(
                "0", DEFAULT_ADMIN, passwordHasher.hash(DEFAULT_ADMIN_PASSWORD), List.of("admin")));
    }

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        return Optional.ofNullable(accounts.get(username));
    }

    @Override
    public boolean exists(String username) {
        return accounts.containsKey(username);
    }

    @Override
    public UserAccount create(String userId, String username, String password, Collection<String> roles) {
        List<String> roleList = roles == null || roles.isEmpty()
                ? List.of("user") : List.copyOf(roles);
        UserAccount account = new UserAccount(userId, username, passwordHasher.hash(password), roleList);
        UserAccount existing = accounts.putIfAbsent(username, account);
        if (existing != null) {
            throw new IllegalArgumentException("用户已存在: " + username);
        }
        return account;
    }
}
