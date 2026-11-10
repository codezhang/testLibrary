package org.example.shell.command;

import org.example.core.KernelResp;
import org.example.core.UserService;
import org.example.core.auth.annotation.RequireLogin;
import org.example.core.auth.annotation.RequirePriority;
import org.example.core.generated.model.UserDTO;
import org.example.core.generated.model.UserSaveReq;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

@Command
@Component
@RequireLogin
public class UserCommands {

    private final UserService userService;

    public UserCommands(UserService userService) {
        this.userService = userService;
    }

    @Command(command = "getUser", description = "Get user by id")
    public String getUser(@Option(longNames = "req", required = true, description = "user id") Long req) {
        KernelResp<UserDTO> response = userService.getUser(req);
        return "code=" + response.getCode()
                + ", msg=" + response.getMsg()
                + ", data=" + format(response.getData());
    }

    @Command(command = "saveUser", description = "Save user")
    @RequirePriority({"admin"})
    public String saveUser(
            @Option(longNames = "userId", required = true, description = "user id") Long userId,
            @Option(longNames = "username", required = true, description = "username") String username) {
        UserSaveReq request = new UserSaveReq();
        request.setUserId(userId);
        request.setUsername(username);

        KernelResp<Void> response = userService.saveUser(request);
        return "code=" + response.getCode() + ", msg=" + response.getMsg();
    }

    private String format(UserDTO user) {
        return user == null ? "null" : user.getUserId() + "/" + user.getUsername();
    }
}
