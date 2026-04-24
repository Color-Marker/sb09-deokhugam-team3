package com.sb09.deokhugam.domain.user.service.basic;

import com.sb09.deokhugam.domain.user.dto.Response.UserResponse;
import com.sb09.deokhugam.domain.user.dto.request.UserLoginRequest;
import com.sb09.deokhugam.domain.user.dto.request.UserRegisterRequest;
import com.sb09.deokhugam.domain.user.dto.request.UserUpdateRequest;
import com.sb09.deokhugam.domain.user.entity.Users;
import com.sb09.deokhugam.domain.user.mapper.UserMapper;
import com.sb09.deokhugam.domain.user.repository.UserRepository;
import com.sb09.deokhugam.domain.user.service.UserService;
import com.sb09.deokhugam.global.exception.user.DuplicateEmailException;
import com.sb09.deokhugam.global.exception.user.InvalidUserCredentialsException;
import com.sb09.deokhugam.global.exception.user.UnauthorizedAccessException;
import com.sb09.deokhugam.global.exception.user.UserNotFoundException;
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
  public UserResponse create(UserRegisterRequest request) {
    if (userRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
      throw new DuplicateEmailException();
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

  //세션이나 토큰을 DB에 저장하는 방식이라면 별도 테이블이 필요하지만,
  //요구사항에서 로그인은 users 테이블에서 이메일로 조회 → 비밀번호 비교 → userId를 클라이언트에 반환만 하면 끝임
  @Override
  @Transactional(readOnly = true)
  public UserResponse login(UserLoginRequest request) {
    Users user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
        .orElseThrow(() -> new InvalidUserCredentialsException());

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new InvalidUserCredentialsException();
    }
    log.info("사용자 로그인 완료: {}", user.getId());

    return userMapper.toDto(user);
  }

  //단건조회이고, 조회는 자주 호출되니까 로깅 보통 생략함
  //UserNotFoundException 생성자가 아닌 withId 메서드를 쓴 이유는
  //어떤 유저가 없다는건지 나중에 디버깅할 때 더 많은 정보를 알려고
  @Override
  @Transactional(readOnly = true)
  public UserResponse findById(UUID id) {
    Users user = userRepository.findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> UserNotFoundException.withId(id));
    if (user.getDeletedAt() != null) {
      throw UserNotFoundException.withId(user.getId());
    }
    return userMapper.toDto(user);
  }

  @Override
  @Transactional
  public UserResponse update(UUID requestUserId, UUID targetId, UserUpdateRequest request) {
    if (!requestUserId.equals(targetId)) {
      throw new UnauthorizedAccessException();
    }

    Users user = userRepository.findByIdAndDeletedAtIsNull(targetId)
        .orElseThrow(() -> UserNotFoundException.withId(targetId));

    if (user.getDeletedAt() != null) {
      throw UserNotFoundException.withId(user.getId());
    }

    user.updateNickname(request.nickname());
    log.info("사용자 닉네임 수정 완료: {}", targetId);
    return userMapper.toDto(user);
  }

  @Override
  @Transactional
  public void softDelete(UUID requestUserId, UUID targetId) {
    if (!requestUserId.equals(targetId)) {
      throw new UnauthorizedAccessException();
    }

    Users user = userRepository.findByIdAndDeletedAtIsNull(targetId)
        .orElseThrow(() -> UserNotFoundException.withId(targetId));

    if (user.getDeletedAt() != null) {
      throw UserNotFoundException.withId(user.getId());
    }

    user.markAsDeleted();
    log.info("사용자 논리 삭제 마킹 완료: {}", targetId);
  }

  @Override
  @Transactional
  public void hardDelete(UUID id) {
    if (!userRepository.existsById(id)) {
      throw new UserNotFoundException();
    }
    userRepository.deleteById(id);
    log.info("사용자 물리 삭제 완료: {}", id);
  }
}
