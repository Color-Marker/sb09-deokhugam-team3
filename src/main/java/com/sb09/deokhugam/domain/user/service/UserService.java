package com.sb09.deokhugam.domain.user.service;

import com.sb09.deokhugam.domain.book.dto.PopularBookDto;
import com.sb09.deokhugam.domain.dashboard.entity.PeriodType;
import com.sb09.deokhugam.domain.user.dto.Response.PowerUserDto;
import com.sb09.deokhugam.domain.user.dto.Response.UserResponse;
import com.sb09.deokhugam.domain.user.dto.request.UserLoginRequest;
import com.sb09.deokhugam.domain.user.dto.request.UserRegisterRequest;
import com.sb09.deokhugam.domain.user.dto.request.UserUpdateRequest;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import java.time.LocalDateTime;
import java.util.UUID;

public interface UserService {

  UserResponse create(UserRegisterRequest request);

  UserResponse login(UserLoginRequest request);

  UserResponse findById(UUID id);

  UserResponse update(UUID requestUserId, UUID targetId, UserUpdateRequest request);

  void softDelete(UUID requestUserId, UUID targetId);

  void hardDelete(UUID id);

  CursorPageResponseDto<PowerUserDto> getPowerUsers(
      PeriodType period,
      Long cursor,
      LocalDateTime after,
      int limit
  );

}
