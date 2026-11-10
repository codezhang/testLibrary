package org.example.core.auth.config;

import org.example.core.auth.aspect.AuthorizationAspect;
import org.example.core.auth.port.CurrentUserProvider;
import org.example.core.auth.port.PasswordHasher;
import org.example.core.auth.port.SessionService;
import org.example.core.auth.port.SessionStore;
import org.example.core.auth.port.UserAccountStore;
import org.example.core.auth.service.BCryptPasswordHasher;
import org.example.core.auth.service.DefaultAdminUserProvider;
import org.example.core.auth.service.DefaultSessionService;
import org.example.core.auth.service.InMemoryUserAccountStore;
import org.example.core.auth.service.SpringSessionStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.MapSession;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SessionRepository;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AuthCoreConfiguration {

    public static final Duration DEFAULT_SESSION_TTL = Duration.ofMinutes(30);

    @Bean
    public SessionRepository<MapSession> sessionRepository() {
        MapSessionRepository repository = new MapSessionRepository(new ConcurrentHashMap<>());
        repository.setDefaultMaxInactiveInterval(DEFAULT_SESSION_TTL);
        return repository;
    }

    @Bean
    public SessionStore sessionStore(SessionRepository<MapSession> sessionRepository) {
        return new SpringSessionStore(sessionRepository, DEFAULT_SESSION_TTL);
    }

    @Bean
    public PasswordHasher passwordHasher() {
        return new BCryptPasswordHasher();
    }

    @Bean
    public UserAccountStore inMemoryUserAccountStore(PasswordHasher passwordHasher) {
        return new InMemoryUserAccountStore(passwordHasher);
    }

    @Bean
    public CurrentUserProvider currentUserProvider(
            SessionStore sessionStore,
            @Value("${kernel.auth.default-admin:true}") boolean defaultAdminEnabled) {
        return new DefaultAdminUserProvider(sessionStore, defaultAdminEnabled);
    }

    @Bean
    public SessionService sessionService(CurrentUserProvider currentUserProvider,
                                         SessionStore sessionStore) {
        return new DefaultSessionService(currentUserProvider, sessionStore, DEFAULT_SESSION_TTL);
    }

    @Bean
    public AuthorizationAspect authorizationAspect(CurrentUserProvider currentUserProvider) {
        return new AuthorizationAspect(currentUserProvider);
    }
}
