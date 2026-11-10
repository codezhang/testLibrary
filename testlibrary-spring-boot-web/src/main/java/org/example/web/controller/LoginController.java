package org.example.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.core.KernelResp;
import org.example.core.auth.exception.NotLoggedInException;
import org.example.core.auth.model.SessionUser;
import org.example.core.auth.model.UserAccount;
import org.example.core.auth.port.PasswordHasher;
import org.example.core.auth.port.SessionService;
import org.example.core.auth.port.SessionStore;
import org.example.core.auth.port.UserAccountStore;
import org.example.web.dto.LoginRequest;
import org.example.web.dto.LoginResponse;
import org.example.web.session.SessionUserFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api")
public class LoginController {

    private static final Duration SESSION_TTL = Duration.ofMinutes(30);

    private final SessionStore sessionStore;
    private final SessionService sessionService;
    private final UserAccountStore userAccountStore;
    private final PasswordHasher passwordHasher;

    public LoginController(SessionStore sessionStore,
                           SessionService sessionService,
                           UserAccountStore userAccountStore,
                           PasswordHasher passwordHasher) {
        this.sessionStore = sessionStore;
        this.sessionService = sessionService;
        this.userAccountStore = userAccountStore;
        this.passwordHasher = passwordHasher;
    }

    @PostMapping("/login")
    public KernelResp<LoginResponse> login(@RequestBody LoginRequest request,
                                           HttpServletResponse response) {
        if (request.username() == null || request.username().isBlank()
                || request.password() == null || request.password().isBlank()) {
            return KernelResp.error(400, "用户名和密码不能为空");
        }

        String username = request.username().trim();
        UserAccount account = userAccountStore.findByUsername(username).orElse(null);
        if (account == null || !passwordHasher.matches(request.password(), account.getPasswordHash())) {
            return KernelResp.error(401, "用户名或密码错误");
        }

        SessionUser user = new SessionUser(account.getUserId(), account.getUsername(), account.getRoleList());
        String sessionId = sessionStore.create(user, SESSION_TTL);
        response.addHeader(HttpHeaders.SET_COOKIE, sessionCookie(sessionId, SESSION_TTL).toString());
        return KernelResp.ok(toResponse(user));
    }

    @PostMapping("/logout")
    public KernelResp<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String sessionId = SessionUserFilter.readSessionId(request);
        if (sessionId != null) {
            sessionStore.remove(sessionId);
        }
        response.addHeader(HttpHeaders.SET_COOKIE, sessionCookie("", Duration.ZERO).toString());
        return KernelResp.ok(null);
    }

    @GetMapping("/me")
    public KernelResp<LoginResponse> me() {
        SessionUser user = sessionService.currentUser();
        if (user == null) {
            throw new NotLoggedInException("未登录或会话已失效");
        }
        return KernelResp.ok(toResponse(user));
    }

    private ResponseCookie sessionCookie(String value, Duration maxAge) {
        return ResponseCookie.from(SessionUserFilter.COOKIE_NAME, value)
                .httpOnly(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(maxAge)
                .build();
    }

    private LoginResponse toResponse(SessionUser user) {
        return new LoginResponse(user.getUserId(), user.getUsername(), user.getRoleList());
    }
}
