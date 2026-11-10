package org.example.core.auth.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.core.auth.annotation.RequireLogin;
import org.example.core.auth.annotation.RequirePriority;
import org.example.core.auth.exception.InsufficientPriorityException;
import org.example.core.auth.exception.NotLoggedInException;
import org.example.core.auth.model.SessionUser;
import org.example.core.auth.port.CurrentUserProvider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Aspect
public class AuthorizationAspect {

    private final CurrentUserProvider currentUserProvider;

    public AuthorizationAspect(CurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    @Around("@annotation(org.example.core.auth.annotation.RequireLogin)"
            + " || @within(org.example.core.auth.annotation.RequireLogin)"
            + " || @annotation(org.example.core.auth.annotation.RequirePriority)"
            + " || @within(org.example.core.auth.annotation.RequirePriority)")
    public Object check(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = joinPoint.getTarget().getClass();

        RequireLogin requireLogin = findAnnotation(method, targetClass, RequireLogin.class);
        RequirePriority requirePriority = findAnnotation(method, targetClass, RequirePriority.class);

        if (requireLogin == null && requirePriority == null) {
            return joinPoint.proceed();
        }

        SessionUser user = currentUserProvider.currentUser();
        if (user == null) {
            throw new NotLoggedInException("未登录或会话已失效");
        }

        if (requirePriority != null) {
            String[] roles = requirePriority.value();
            if (roles.length > 0 && !user.hasAnyRole(java.util.List.of(roles))) {
                throw new InsufficientPriorityException(
                        "缺少所需角色: " + String.join(",", roles));
            }
        }

        return joinPoint.proceed();
    }

    private static <A extends Annotation> A findAnnotation(Method method,
                                                           Class<?> targetClass,
                                                           Class<A> annotationType) {
        A annotation = method.getAnnotation(annotationType);
        if (annotation != null) {
            return annotation;
        }
        return targetClass.getAnnotation(annotationType);
    }
}
