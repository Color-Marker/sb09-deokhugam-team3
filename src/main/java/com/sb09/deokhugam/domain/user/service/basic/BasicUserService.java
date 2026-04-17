package com.sb09.deokhugam.domain.user.service.basic;

import com.sb09.deokhugam.domain.user.dto.Response.UserResponse;
import com.sb09.deokhugam.domain.user.dto.request.UserLoginRequest;
import com.sb09.deokhugam.domain.user.dto.request.UserRegisterRequest;
import com.sb09.deokhugam.domain.user.dto.request.UserUpdateRequest;
import com.sb09.deokhugam.domain.user.entity.Users;
import com.sb09.deokhugam.domain.user.mapper.UserMapper;
import com.sb09.deokhugam.domain.user.repository.UserRepository;
import com.sb09.deokhugam.domain.user.service.UserService;
import com.sb09.deokhugam.global.Exception.CustomException;
import com.sb09.deokhugam.global.Exception.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public UserResponse register(UserRegisterRequest request) {
    if (userRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
      throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
    }

    Users user = Users.builder()
        .email(request.email())
        .nickname(request.nickname())
        .password(passwordEncoder.encode(request.password()))
        .build();

    Users savedUser = userRepository.save(user);
    log.info("신규 사용자 가입 완료: {}", savedUser.getId());

    return userMapper.toDto(savedUser);
  }

  @Override
  @Transactional(readOnly = true)
  public UserResponse login(UserLoginRequest request) {
    Users user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_USER_CREDENTIALS));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new CustomException(ErrorCode.INVALID_USER_CREDENTIALS);
    }

    return userMapper.toDto(user);
  }

  @Override
  @Transactional(readOnly = true)
  public UserResponse getUser(UUID id) {
    Users user = userRepository.findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    return userMapper.toDto(user);
  }

  @Override
  @Transactional
  public UserResponse updateNickname(UUID requestUserId, UUID targetId, UserUpdateRequest request) {
    if (!requestUserId.equals(targetId)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
    }

    Users user = userRepository.findByIdAndDeletedAtIsNull(targetId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    user.updateNickname(request.nickname());
    return userMapper.toDto(user);
  }

  @Override
  @Transactional
  public void softDelete(UUID requestUserId, UUID targetId) {
    if (!requestUserId.equals(targetId)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
    }

    Users user = userRepository.findByIdAndDeletedAtIsNull(targetId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    user.markAsDeleted();
    log.info("사용자 논리 삭제 마킹 완료: {}", targetId);
  }

  @Override
  @Transactional
  public void hardDelete(UUID id) {
    if (!userRepository.existsById(id)) {
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }
    userRepository.deleteById(id);
    log.info("사용자 물리 삭제 완료: {}", id);
  }
}
