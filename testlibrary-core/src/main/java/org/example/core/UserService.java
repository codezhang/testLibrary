package org.example.core;

import org.example.core.generated.model.UserDTO;
import org.example.core.generated.model.UserSaveReq;

public interface UserService {

    KernelResp<UserDTO> getUser(Long req);

    KernelResp<Void> saveUser(UserSaveReq req);
}
