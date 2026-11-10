package org.example.wrapper.common;

import org.example.core.UserService;
import org.example.core.service.InMemoryUserService;
import org.example.persistence.jdbc.JdbcBootstrapConfiguration;
import org.example.wrapper.persistence.InMemoryPersistenceConfiguration;
import org.example.wrapper.persistence.JdbcPersistenceSelectionConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({InMemoryPersistenceConfiguration.class,
        JdbcPersistenceSelectionConfiguration.class,
        JdbcBootstrapConfiguration.class})
public class WrapperCommonConfiguration {

    @Bean
    public UserService userService() {
        return new InMemoryUserService();
    }
}
