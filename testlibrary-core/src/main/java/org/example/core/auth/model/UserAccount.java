package org.example.core.auth.model;

import java.util.List;

public class UserAccount {

    private final String userId;
    private final String username;
    private final String passwordHash;
    private final List<String> roleList;

    public UserAccount(String userId, String username, String passwordHash, List<String> roleList) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.roleList = List.copyOf(roleList);
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public List<String> getRoleList() {
        return roleList;
    }
}
