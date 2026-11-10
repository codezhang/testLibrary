package org.example.web.controller;

import org.example.core.KernelResp;
import org.example.core.UserService;
import org.example.core.auth.annotation.RequireLogin;
import org.example.core.auth.annotation.RequirePriority;
import org.example.core.generated.model.UserDTO;
import org.example.core.generated.model.UserSaveReq;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kernel/user")
@RequireLogin
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/get")
    public KernelResp<UserDTO> getUser(@RequestParam("req") Long req) {
        return userService.getUser(req);
    }

    @PostMapping("/save")
    @RequirePriority({"admin"})
    public KernelResp<Void> saveUser(@RequestBody UserSaveReq req) {
        return userService.saveUser(req);
    }
}
