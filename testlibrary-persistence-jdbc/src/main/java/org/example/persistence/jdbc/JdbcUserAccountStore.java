package org.example.persistence.jdbc;

import org.example.core.auth.model.UserAccount;
import org.example.core.auth.port.PasswordHasher;
import org.example.core.auth.port.UserAccountStore;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class JdbcUserAccountStore implements UserAccountStore {

    private static final String ADMIN = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    private final DataSource dataSource;
    private final PasswordHasher passwordHasher;

    public JdbcUserAccountStore(DataSource dataSource, PasswordHasher passwordHasher) {
        this.dataSource = dataSource;
        this.passwordHasher = passwordHasher;
        if (!exists(ADMIN)) {
            create("0", ADMIN, ADMIN_PASSWORD, List.of("admin"));
        }
    }

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        String sql = "SELECT user_id, username, password_hash, roles FROM user_account WHERE username = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new BookingPersistenceException("query user failed", e);
        }
    }

    @Override
    public boolean exists(String username) {
        String sql = "SELECT 1 FROM user_account WHERE username = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new BookingPersistenceException("query user failed", e);
        }
    }

    @Override
    public UserAccount create(String userId, String username, String password, Collection<String> roles) {
        if (exists(username)) {
            throw new IllegalArgumentException("用户已存在: " + username);
        }
        List<String> roleList = roles == null || roles.isEmpty()
                ? List.of("user") : List.copyOf(roles);
        String passwordHash = passwordHasher.hash(password);
        String sql = "INSERT INTO user_account (user_id, username, password_hash, roles) VALUES (?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.setString(2, username);
            statement.setString(3, passwordHash);
            statement.setString(4, String.join(",", roleList));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new BookingPersistenceException("create user failed", e);
        }
        return new UserAccount(userId, username, passwordHash, roleList);
    }

    private UserAccount map(ResultSet rs) throws SQLException {
        List<String> roles = Arrays.stream(rs.getString("roles").split(","))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .toList();
        return new UserAccount(
                rs.getString("user_id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                roles);
    }
}
