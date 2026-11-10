package org.example.core.auth.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SessionUser implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_ADMIN_ID = "0";
    public static final String DEFAULT_ADMIN_NAME = "admin";
    public static final String ROLE_ADMIN = "admin";

    private String userId;
    private String username;
    private List<String> roleList = new ArrayList<>();

    public SessionUser() {
    }

    public SessionUser(String userId, String username, Collection<String> roles) {
        this.userId = userId;
        this.username = username;
        setRoleList(roles);
    }

    public static SessionUser defaultAdmin() {
        return new SessionUser(DEFAULT_ADMIN_ID, DEFAULT_ADMIN_NAME, List.of(ROLE_ADMIN));
    }

    public boolean hasAnyRole(Collection<String> requiredRoles) {
        if (requiredRoles == null || requiredRoles.isEmpty()) {
            return true;
        }
        return requiredRoles.stream().anyMatch(roleList::contains);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoleList() {
        return roleList;
    }

    public void setRoleList(Collection<String> roles) {
        this.roleList = roles == null ? new ArrayList<>() : new ArrayList<>(roles);
    }
}
