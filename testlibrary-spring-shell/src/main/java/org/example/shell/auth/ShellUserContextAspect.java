package org.example.shell.auth;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.core.auth.SessionContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class ShellUserContextAspect {

    private final ShellUserSession shellUserSession;

    public ShellUserContextAspect(ShellUserSession shellUserSession) {
        this.shellUserSession = shellUserSession;
    }

    @Around("@annotation(org.springframework.shell.command.annotation.Command)"
            + " || @within(org.springframework.shell.command.annotation.Command)")
    public Object applyCurrentUser(ProceedingJoinPoint joinPoint) throws Throwable {
        SessionContext.setUser(shellUserSession.currentOrDefault());
        try {
            return joinPoint.proceed();
        } finally {
            SessionContext.clearUser();
        }
    }
}
