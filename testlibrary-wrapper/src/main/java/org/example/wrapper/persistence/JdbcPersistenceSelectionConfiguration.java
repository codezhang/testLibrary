package org.example.wrapper.persistence;

import org.example.persistence.jdbc.JdbcPersistenceConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnProperty(name = "library.persistence", havingValue = "jdbc")
@Import(JdbcPersistenceConfiguration.class)
public class JdbcPersistenceSelectionConfiguration {
}
