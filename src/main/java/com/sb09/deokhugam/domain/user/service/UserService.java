package com.sb09.deokhugam.domain.user.service;

import com.sb09.deokhugam.domain.user.dto.Response.UserResponse;
import com.sb09.deokhugam.domain.user.dto.request.UserRegisterRequest;
import java.util.UUID;

public interface UserService {

  UserResponse register(UserRegisterRequest request);

  UserResponse getUser(UUID id);
}
