package com.sb09.deokhugam.domain.user.service.impl;


import com.sb09.deokhugam.domain.user.dto.Response.UserResponse;
import com.sb09.deokhugam.domain.user.dto.request.UserRegisterRequest;
import com.sb09.deokhugam.domain.user.entity.Users;
import com.sb09.deokhugam.domain.user.mapper.UserMapper;
import com.sb09.deokhugam.domain.user.repository.UserRepository;
import com.sb09.deokhugam.domain.user.service.UserService;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Override
  @Transactional
  public UserResponse register(UserRegisterRequest request) {
    // 1. 중복 이메일 검증
    if (userRepository.existsByEmail(request.email())) {
      log.error("중복된 이메일 가입 시도: {}", request.email());
      throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
    }

    // 2. DTO -> Entity 변환 및 빌더로 생성
    Users user = Users.builder()
        .email(request.email())
        .nickname(request.nickname())
        .password(request.password())
        .build();

    // 3. DB 저장
    Users savedUser = userRepository.save(user);
    log.info("신규 유저 가입 완료: ID={}", savedUser.getId());

    // 4. Entity -> DTO 변환하여 반환
    return userMapper.toDto(savedUser);
  }

  @Override
  @Transactional(readOnly = true)
  public UserResponse getUser(UUID id) {
    Users user = userRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("해당 유저를 찾을 수 없습니다."));

    return userMapper.toDto(user);
  }
}
