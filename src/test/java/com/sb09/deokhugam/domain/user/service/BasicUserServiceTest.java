package com.sb09.deokhugam.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.sb09.deokhugam.domain.dashboard.entity.PeriodType;
import com.sb09.deokhugam.domain.dashboard.entity.PowerUser;
import com.sb09.deokhugam.domain.dashboard.repository.PowerUserRepository;
import com.sb09.deokhugam.domain.user.dto.Response.PowerUserDto;
import com.sb09.deokhugam.domain.user.dto.Response.UserResponse;
import com.sb09.deokhugam.domain.user.dto.request.UserLoginRequest;
import com.sb09.deokhugam.domain.user.dto.request.UserRegisterRequest;
import com.sb09.deokhugam.domain.user.dto.request.UserUpdateRequest;
import com.sb09.deokhugam.domain.user.entity.Users;
import com.sb09.deokhugam.domain.user.mapper.UserMapper;
import com.sb09.deokhugam.domain.user.repository.UserRepository;
import com.sb09.deokhugam.domain.user.service.basic.BasicUserService;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import com.sb09.deokhugam.global.exception.ErrorCode;
import com.sb09.deokhugam.global.exception.user.DuplicateEmailException;
import com.sb09.deokhugam.global.exception.user.InvalidUserCredentialsException;
import com.sb09.deokhugam.global.exception.user.UnauthorizedAccessException;
import com.sb09.deokhugam.global.exception.user.UserNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BasicUserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private PowerUserRepository powerUserRepository;

  @InjectMocks
  private BasicUserService userService;

  private String email;
  private String nickname;
  private String newNickname;
  private String password;
  private String encodedPassword;

  private UUID userId;
  private Users user;
  private UserResponse userResponse;

  @BeforeEach
  void setUp() {
    email = "test@test.com";
    nickname = "테스트유저";
    newNickname = "새닉네임";
    password = "Test1234!";
    encodedPassword = "encodedPassword";

    userId = UUID.randomUUID();
    user = mock(Users.class);

    given(user.getId()).willReturn(userId);
    given(user.getEmail()).willReturn(email);
    given(user.getNickname()).willReturn(nickname);
    given(user.getPassword()).willReturn(encodedPassword);
    given(user.getDeletedAt()).willReturn(null);

    userResponse = new UserResponse(userId, email, nickname, LocalDateTime.now());
  }

  // ===== create =====

  @Test
  @DisplayName("회원가입 성공")
  void create_success() {
    UserRegisterRequest request = new UserRegisterRequest(email, nickname, password);

    given(userRepository.existsByEmailAndDeletedAtIsNull(request.email())).willReturn(false);
    given(passwordEncoder.encode(request.password())).willReturn(encodedPassword);
    given(userRepository.save(any(Users.class))).willReturn(user);
    given(userMapper.toDto(user)).willReturn(userResponse);

    UserResponse result = userService.create(request);

    assertThat(result).isEqualTo(userResponse);
    verify(userRepository).save(any(Users.class));
  }

  @Test
  @DisplayName("회원가입 실패 - 이메일 중복")
  void create_duplicateEmail() {
    UserRegisterRequest request = new UserRegisterRequest(email, nickname, password);

    given(userRepository.existsByEmailAndDeletedAtIsNull(request.email())).willReturn(true);

    assertThatThrownBy(() -> userService.create(request))
        .isInstanceOf(DuplicateEmailException.class)
        .satisfies(e -> assertThat(((DuplicateEmailException) e).getErrorCode())
            .isEqualTo(ErrorCode.DUPLICATE_EMAIL));
  }

  // ===== login =====

  @Test
  @DisplayName("로그인 성공")
  void login_success() {
    UserLoginRequest request = new UserLoginRequest(email, password);

    given(userRepository.findByEmailAndDeletedAtIsNull(request.email())).willReturn(
        Optional.of(user));
    given(passwordEncoder.matches(request.password(), user.getPassword())).willReturn(true);
    given(userMapper.toDto(user)).willReturn(userResponse);

    UserResponse result = userService.login(request);

    assertThat(result).isEqualTo(userResponse);
  }

  @Test
  @DisplayName("로그인 실패 - 존재하지 않는 이메일")
  void login_emailNotFound() {
    // 존재하지 않음을 명시하기 위해 변수 조합("none" + email) 사용
    String nonExistentEmail = "none_" + email;
    UserLoginRequest request = new UserLoginRequest(nonExistentEmail, password);

    given(userRepository.findByEmailAndDeletedAtIsNull(request.email())).willReturn(
        Optional.empty());

    assertThatThrownBy(() -> userService.login(request))
        .isInstanceOf(InvalidUserCredentialsException.class)
        .satisfies(e -> assertThat(((InvalidUserCredentialsException) e).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_USER_CREDENTIALS));
  }

  @Test
  @DisplayName("로그인 실패 - 비밀번호 불일치")
  void login_wrongPassword() {
    UserLoginRequest request = new UserLoginRequest(email, "wrong_" + password);

    given(userRepository.findByEmailAndDeletedAtIsNull(request.email())).willReturn(
        Optional.of(user));
    // passwordEncoder가 false를 반환하도록 설정함
    given(passwordEncoder.matches(request.password(), user.getPassword())).willReturn(false);

    assertThatThrownBy(() -> userService.login(request))
        .isInstanceOf(InvalidUserCredentialsException.class)
        .satisfies(e -> assertThat(((InvalidUserCredentialsException) e).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_USER_CREDENTIALS));
  }

  // ===== findById =====

  @Test
  @DisplayName("사용자 단건 조회 성공")
  void findById_success() {
    given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));
    given(userMapper.toDto(user)).willReturn(userResponse);

    UserResponse result = userService.findById(userId);

    assertThat(result).isEqualTo(userResponse);
  }

  @Test
  @DisplayName("사용자 단건 조회 실패 - 존재하지 않는 사용자")
  void findById_notFound() {
    UUID notExistId = UUID.randomUUID();
    given(userRepository.findByIdAndDeletedAtIsNull(notExistId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> userService.findById(notExistId))
        .isInstanceOf(UserNotFoundException.class)
        .satisfies(e -> assertThat(((UserNotFoundException) e).getErrorCode())
            .isEqualTo(ErrorCode.USER_NOT_FOUND));
  }

  // ===== update =====

  @Test
  @DisplayName("닉네임 수정 성공")
  void update_success() {
    UserUpdateRequest request = new UserUpdateRequest(newNickname);

    given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));
    given(userMapper.toDto(user)).willReturn(userResponse);

    UserResponse result = userService.update(userId, userId, request);

    assertThat(result).isEqualTo(userResponse);
    verify(user).updateNickname(request.nickname());
  }

  @Test
  @DisplayName("닉네임 수정 실패 - 본인 아닌 사용자 수정 시도")
  void update_unauthorized() {
    UUID otherUserId = UUID.randomUUID();
    UserUpdateRequest request = new UserUpdateRequest(newNickname);

    assertThatThrownBy(() -> userService.update(otherUserId, userId, request))
        .isInstanceOf(UnauthorizedAccessException.class)
        .satisfies(e -> assertThat(((UnauthorizedAccessException) e).getErrorCode())
            .isEqualTo(ErrorCode.UNAUTHORIZED_ACCESS));
  }

  @Test
  @DisplayName("닉네임 수정 실패 - 존재하지 않는 사용자")
  void update_notFound() {
    UUID notExistId = UUID.randomUUID();
    UserUpdateRequest request = new UserUpdateRequest(newNickname);
    given(userRepository.findByIdAndDeletedAtIsNull(notExistId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> userService.update(notExistId, notExistId, request))
        .isInstanceOf(UserNotFoundException.class)
        .satisfies(e -> assertThat(((UserNotFoundException) e).getErrorCode())
            .isEqualTo(ErrorCode.USER_NOT_FOUND));
  }

  // ===== softDelete =====

  @Test
  @DisplayName("논리 삭제 성공")
  void softDelete_success() {
    given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));

    userService.softDelete(userId, userId);

    verify(user).markAsDeleted();
  }

  @Test
  @DisplayName("논리 삭제 실패 - 본인 아닌 사용자 삭제 시도")
  void softDelete_unauthorized() {
    UUID otherUserId = UUID.randomUUID();

    assertThatThrownBy(() -> userService.softDelete(otherUserId, userId))
        .isInstanceOf(UnauthorizedAccessException.class)
        .satisfies(e -> assertThat(((UnauthorizedAccessException) e).getErrorCode())
            .isEqualTo(ErrorCode.UNAUTHORIZED_ACCESS));
  }

  @Test
  @DisplayName("논리 삭제 실패 - 존재하지 않는 사용자")
  void softDelete_notFound() {
    UUID notExistId = UUID.randomUUID();
    given(userRepository.findByIdAndDeletedAtIsNull(notExistId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> userService.softDelete(notExistId, notExistId))
        .isInstanceOf(UserNotFoundException.class)
        .satisfies(e -> assertThat(((UserNotFoundException) e).getErrorCode())
            .isEqualTo(ErrorCode.USER_NOT_FOUND));
  }

  // ===== hardDelete =====

  @Test
  @DisplayName("물리 삭제 성공")
  void hardDelete_success() {
    given(userRepository.existsById(userId)).willReturn(true);

    userService.hardDelete(userId);

    verify(userRepository).deleteById(userId);
  }

  @Test
  @DisplayName("물리 삭제 실패 - 존재하지 않는 사용자")
  void hardDelete_notFound() {
    UUID notExistId = UUID.randomUUID();
    given(userRepository.existsById(notExistId)).willReturn(false);

    assertThatThrownBy(() -> userService.hardDelete(notExistId))
        .isInstanceOf(UserNotFoundException.class)
        .satisfies(e -> assertThat(((UserNotFoundException) e).getErrorCode())
            .isEqualTo(ErrorCode.USER_NOT_FOUND));
  }

  @Test
  @DisplayName("파워 유저 목록 조회 성공")
  void getPowerUsers_success() {
    PeriodType period = PeriodType.WEEKLY;
    LocalDate baseDate = LocalDate.now();

    PowerUser mockPowerUser = mock(PowerUser.class);
    given(mockPowerUser.getRanking()).willReturn(1L);
    given(mockPowerUser.getBaseDate()).willReturn(baseDate);
    given(mockPowerUser.getPeriod()).willReturn(period);
    given(mockPowerUser.getUserId()).willReturn(userId);
    given(mockPowerUser.getCreatedAt()).willReturn(LocalDateTime.now());

    given(powerUserRepository.findTopByPeriodOrderByBaseDateDesc(period))
        .willReturn(Optional.of(mockPowerUser));
    given(powerUserRepository.findAll())
        .willReturn(List.of(mockPowerUser));
    given(userRepository.findAllById(any()))
        .willReturn(List.of(user));

    CursorPageResponseDto<PowerUserDto> result =
        userService.getPowerUsers(period, null, null, 10);

    assertThat(result.content()).hasSize(1);
    assertThat(result.hasNext()).isFalse();
  }

  @Test
  @DisplayName("파워 유저 목록 조회 - 데이터 없을 때 빈 리스트 반환")
  void getPowerUsers_empty() {
    PeriodType period = PeriodType.WEEKLY;

    given(powerUserRepository.findTopByPeriodOrderByBaseDateDesc(period))
        .willReturn(Optional.empty());
    given(powerUserRepository.findAll())
        .willReturn(List.of());

    CursorPageResponseDto<PowerUserDto> result =
        userService.getPowerUsers(period, null, null, 10);

    assertThat(result.content()).isEmpty();
    assertThat(result.hasNext()).isFalse();
  }
}
