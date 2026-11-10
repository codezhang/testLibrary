package org.example.core.service;

import org.example.core.KernelResp;
import org.example.core.UserService;
import org.example.core.generated.model.UserDTO;
import org.example.core.generated.model.UserSaveReq;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryUserService implements UserService {

    private final Map<Long, UserDTO> users = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1002);

    public InMemoryUserService() {
        UserDTO seed = new UserDTO();
        seed.setUserId(1001L);
        seed.setUsername("shared-seed");
        seed.setAge(20);
        users.put(seed.getUserId(), seed);
    }

    @Override
    public KernelResp<UserDTO> getUser(Long req) {
        return KernelResp.ok(users.get(req));
    }

    @Override
    public KernelResp<Void> saveUser(UserSaveReq req) {
        long userId = req.getUserId() == null ? sequence.getAndIncrement() : req.getUserId();
        UserDTO user = new UserDTO();
        user.setUserId(userId);
        user.setUsername(req.getUsername());
        users.put(userId, user);
        return KernelResp.ok(null);
    }
}
