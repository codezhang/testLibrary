package org.example.persistence.jdbc;

import org.example.core.persistence.DatabaseBootstrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JdbcBootstrapConfiguration {

    @Bean
    public DatabaseBootstrapper databaseBootstrapper() {
        return new DuckDbBootstrapper();
    }
}
