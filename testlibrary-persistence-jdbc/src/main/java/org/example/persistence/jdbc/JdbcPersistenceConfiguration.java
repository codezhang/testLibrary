package org.example.persistence.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.example.core.auth.port.PasswordHasher;
import org.example.core.auth.port.UserAccountStore;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@MapperScan("org.example.persistence.jdbc.mybatis.mapper")
public class JdbcPersistenceConfiguration {

    @Bean(destroyMethod = "close")
    public HikariDataSource bookingDataSource(
            @Value("${library.jdbc.url}") String url,
            @Value("${library.jdbc.username:}") String username,
            @Value("${library.jdbc.password:}") String password,
            @Value("${library.jdbc.driver-class-name:}") String driverClassName,
            @Value("${library.jdbc.pool-size:5}") int poolSize,
            @Value("${library.jdbc.init-schema:false}") boolean initSchema) {

        prepareDuckDbDirectory(url);

        HikariConfig config = new HikariConfig();
        config.setPoolName("booking-pool");
        config.setJdbcUrl(url);
        if (!username.isBlank()) {
            config.setUsername(username);
        }
        if (!password.isBlank()) {
            config.setPassword(password);
        }
        if (!driverClassName.isBlank()) {
            config.setDriverClassName(driverClassName);
        }
        config.setMaximumPoolSize(Math.max(1, poolSize));
        config.setMinimumIdle(1);

        HikariDataSource dataSource = new HikariDataSource(config);
        if (initSchema) {
            new JdbcSchemaInitializer(dataSource).initialize();
        }
        return dataSource;
    }

    @Bean
    @Primary
    public UserAccountStore jdbcUserAccountStore(DataSource dataSource, PasswordHasher passwordHasher) {
        return new JdbcUserAccountStore(dataSource, passwordHasher);
    }

    private void prepareDuckDbDirectory(String url) {
        String prefix = "jdbc:duckdb:";
        if (!url.startsWith(prefix)) {
            return;
        }
        String database = url.substring(prefix.length());
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
            throw new BookingPersistenceException("无法创建 DuckDB 目录: " + parent, e);
        }
    }
}
