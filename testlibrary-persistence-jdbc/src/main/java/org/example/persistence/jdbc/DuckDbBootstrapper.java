package org.example.persistence.jdbc;

import org.example.core.persistence.DatabaseBootstrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DuckDbBootstrapper implements DatabaseBootstrapper {

    private static final String PREFIX = "jdbc:duckdb:";

    @Override
    public void initializeEmpty(String jdbcUrl) {
        if (jdbcUrl == null || !jdbcUrl.startsWith(PREFIX)) {
            throw new IllegalArgumentException("expected jdbc:duckdb: url");
        }
        prepareDirectory(jdbcUrl);
        try {
            Class.forName("org.duckdb.DuckDBDriver");
        } catch (ClassNotFoundException e) {
            throw new BookingPersistenceException("duckdb driver not found", e);
        }
        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            JdbcSchemaInitializer.executeScript(connection, JdbcSchemaInitializer.SCHEMA_RESOURCE);
        } catch (SQLException e) {
            throw new BookingPersistenceException("duckdb init failed", e);
        }
    }

    private void prepareDirectory(String jdbcUrl) {
        String database = jdbcUrl.substring(PREFIX.length());
        if (database.isBlank() || ":memory:".equals(database)) {
            return;
        }
        Path parent = Paths.get(database).toAbsolutePath().getParent();
        if (parent == null) {
            return;
        }
        try {
            Files.createDirectories(parent);
        } catch (IOException e) {
            throw new BookingPersistenceException("create duckdb directory failed", e);
        }
    }
}
