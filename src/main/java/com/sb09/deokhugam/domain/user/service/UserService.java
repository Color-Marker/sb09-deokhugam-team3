package com.sb09.deokhugam.domain.user.service;

import com.sb09.deokhugam.domain.user.dto.Response.UserResponse;
import com.sb09.deokhugam.domain.user.dto.request.UserLoginRequest;
import com.sb09.deokhugam.domain.user.dto.request.UserRegisterRequest;
import com.sb09.deokhugam.domain.user.dto.request.UserUpdateRequest;
import java.util.UUID;

public interface UserService {

  UserResponse create(UserRegisterRequest request);

  UserResponse login(UserLoginRequest request);

  UserResponse findById(UUID id);

  UserResponse update(UUID requestUserId, UUID targetId, UserUpdateRequest request);

  void softDelete(UUID requestUserId, UUID targetId);

  void hardDelete(UUID id);
}
