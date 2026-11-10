package org.example.shell.auth;

import org.example.core.UserService;
import org.example.core.auth.annotation.RequirePriority;
import org.example.core.auth.model.SessionUser;
import org.example.core.auth.model.UserAccount;
import org.example.core.auth.port.UserAccountStore;
import org.example.core.generated.model.UserSaveReq;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Command
@Component
public class AuthCommands {

    private final ShellUserSession shellUserSession;
    private final UserAccountStore userAccountStore;
    private final UserService userService;

    public AuthCommands(ShellUserSession shellUserSession,
                        UserAccountStore userAccountStore,
                        UserService userService) {
        this.shellUserSession = shellUserSession;
        this.userAccountStore = userAccountStore;
        this.userService = userService;
    }

    @Command(command = "addUser", description = "Create a user with password (admin)")
    @RequirePriority({"admin"})
    public String addUser(
            @Option(longNames = "userId", required = true) Long userId,
            @Option(longNames = "username", required = true) String username,
            @Option(longNames = "password", required = true) String password,
            @Option(longNames = "roles", defaultValue = "user") String roles) {
        if (username == null || username.isBlank()) {
            return "用户名不能为空";
        }
        if (password == null || password.isBlank()) {
            return "密码不能为空";
        }

        List<String> roleList = parseRoles(roles);
        UserAccount account = userAccountStore.create(
                String.valueOf(userId), username.trim(), password, roleList);

        UserSaveReq req = new UserSaveReq();
        req.setUserId(userId);
        req.setUsername(account.getUsername());
        userService.saveUser(req);

        return "created user=" + account.getUserId() + "/" + account.getUsername()
                + ", roleList=" + account.getRoleList();
    }

    @Command(command = "loginAs", description = "Switch current shell user, e.g. loginAs --username user --roles user")
    public String loginAs(
            @Option(longNames = "username", required = true) String username,
            @Option(longNames = "userId", defaultValue = "") String userId,
            @Option(longNames = "roles", defaultValue = "user") String roles) {

        List<String> roleList = parseRoles(roles);
        String id = (userId == null || userId.isBlank()) ? username : userId;
        SessionUser user = new SessionUser(id, username, roleList);
        shellUserSession.login(user);
        return "current user=" + user.getUserId() + "/" + user.getUsername()
                + ", roleList=" + user.getRoleList();
    }

    private static List<String> parseRoles(String roles) {
        String effectiveRoles = (roles == null || roles.isBlank()) ? "user" : roles;
        return Arrays.stream(effectiveRoles.split(","))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .toList();
    }

    @Command(command = "logout", description = "Logout shell user and fall back to default admin")
    public String logout() {
        shellUserSession.logout();
        return "已登出，当前为默认管理员";
    }

    @Command(command = "whoami", description = "Show current shell user")
    public String whoami() {
        SessionUser user = shellUserSession.currentOrDefault();
        return "user=" + user.getUserId() + "/" + user.getUsername()
                + ", roleList=" + user.getRoleList();
    }
}
