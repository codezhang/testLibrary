package org.example.persistence.jdbc;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcSchemaInitializer {

    public static final String SCHEMA_RESOURCE = "db/postgresql/schema.sql";
    public static final String DATA_RESOURCE = "db/postgresql/data.sql";

    private final DataSource dataSource;

    public JdbcSchemaInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void initialize() {
        initializeSchema();
        initializeData();
    }

    public void initializeSchema() {
        try (Connection connection = dataSource.getConnection()) {
            executeScript(connection, SCHEMA_RESOURCE);
        } catch (SQLException e) {
            throw new BookingPersistenceException("schema init failed", e);
        }
    }

    public void initializeData() {
        try (Connection connection = dataSource.getConnection()) {
            executeScript(connection, DATA_RESOURCE);
        } catch (SQLException e) {
            throw new BookingPersistenceException("data init failed", e);
        }
    }

    public static void executeScript(Connection connection, String resource) throws SQLException {
        String script = read(resource);
        try (Statement statement = connection.createStatement()) {
            for (String sql : script.split(";")) {
                String cleaned = sql.replaceAll("(?m)^\\s*--.*$", "").trim();
                if (!cleaned.isEmpty()) {
                    statement.execute(cleaned);
                }
            }
        }
    }

    private static String read(String resource) {
        try (InputStream input = JdbcSchemaInitializer.class.getClassLoader()
                .getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalStateException("missing resource: " + resource);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BookingPersistenceException("read script failed: " + resource, e);
        }
    }
}
