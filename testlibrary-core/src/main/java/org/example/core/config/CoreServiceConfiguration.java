package org.example.core.config;

import org.example.core.concurrent.LocalLockService;
import org.example.core.concurrent.LockService;
import org.example.core.page.PageHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("org.example.core.service")
public class CoreServiceConfiguration {

    @Bean
    public LockService lockService() {
        return new LocalLockService();
    }

    @Bean
    public PageHelper pageHelper() {
        return new PageHelper();
    }
}
