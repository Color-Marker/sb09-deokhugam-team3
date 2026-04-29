package com.sb09.deokhugam.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sb09.deokhugam.config.WebConfig;
import com.sb09.deokhugam.domain.user.dto.Response.UserResponse;
import com.sb09.deokhugam.domain.user.dto.request.UserLoginRequest;
import com.sb09.deokhugam.domain.user.dto.request.UserRegisterRequest;
import com.sb09.deokhugam.domain.user.dto.request.UserUpdateRequest;
import com.sb09.deokhugam.domain.user.service.UserService;
import com.sb09.deokhugam.global.exception.user.DuplicateEmailException;
import com.sb09.deokhugam.global.exception.user.InvalidUserCredentialsException;
import com.sb09.deokhugam.global.exception.user.UnauthorizedAccessException;
import com.sb09.deokhugam.global.exception.user.UserNotFoundException;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import(WebConfig.class)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  private UUID userId;
  private UserResponse userResponse;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    userResponse = new UserResponse(userId, "test@test.com", "테스트유저", LocalDateTime.now());
  }

  // ===== register =====

  @Test
  @DisplayName("회원가입 성공 - 201 반환")
  void register_success() throws Exception {
    UserRegisterRequest request = new UserRegisterRequest("test@test.com", "테스트유저", "Test1234!");
    given(userService.create(any())).willReturn(userResponse);

    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.email").value("test@test.com"));
  }

  @Test
  @DisplayName("회원가입 실패 - 이메일 형식 오류 - 400 반환")
  void register_invalidEmail_returns400() throws Exception {
    UserRegisterRequest request = new UserRegisterRequest("invalid-email", "테스트유저", "Test1234!");

    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("회원가입 실패 - 비밀번호 형식 오류 - 400 반환")
  void register_invalidPassword_returns400() throws Exception {
    UserRegisterRequest request = new UserRegisterRequest("test@test.com", "테스트유저", "1234");

    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("회원가입 실패 - 이메일 중복 - 409 반환")
  void register_duplicateEmail_returns409() throws Exception {
    UserRegisterRequest request = new UserRegisterRequest("test@test.com", "테스트유저", "Test1234!");
    given(userService.create(any())).willThrow(new DuplicateEmailException());

    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());
  }

  // ===== login =====

  @Test
  @DisplayName("로그인 성공 - 200 반환")
  void login_success() throws Exception {
    UserLoginRequest request = new UserLoginRequest("test@test.com", "Test1234!");
    given(userService.login(any())).willReturn(userResponse);

    mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists());
  }

  @Test
  @DisplayName("로그인 실패 - 이메일 또는 비밀번호 불일치 - 401 반환")
  void login_invalidCredentials_returns401() throws Exception {
    UserLoginRequest request = new UserLoginRequest("test@test.com", "Wrong1234!");
    given(userService.login(any())).willThrow(new InvalidUserCredentialsException());

    mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  // ===== getUser =====

  @Test
  @DisplayName("사용자 조회 성공 - 200 반환")
  void getUser_success() throws Exception {
    given(userService.findById(userId)).willReturn(userResponse);

    mockMvc.perform(get("/api/users/{userId}", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId.toString()));
  }

  @Test
  @DisplayName("사용자 조회 실패 - 존재하지 않는 사용자 - 404 반환")
  void getUser_notFound_returns404() throws Exception {
    given(userService.findById(any())).willThrow(UserNotFoundException.withId(userId));

    mockMvc.perform(get("/api/users/{userId}", UUID.randomUUID()))
        .andExpect(status().isNotFound());
  }

  // ===== update =====

  @Test
  @DisplayName("닉네임 수정 성공 - 200 반환")
  void update_success() throws Exception {
    UserUpdateRequest request = new UserUpdateRequest("새닉네임");
    given(userService.update(any(), any(), any())).willReturn(userResponse);

    mockMvc.perform(patch("/api/users/{userId}", userId)
            .header("Deokhugam-Request-User-ID", userId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("닉네임 수정 실패 - 권한 없음 - 403 반환")
  void update_unauthorized_returns403() throws Exception {
    UserUpdateRequest request = new UserUpdateRequest("새닉네임");
    given(userService.update(any(), any(), any())).willThrow(new UnauthorizedAccessException());

    mockMvc.perform(patch("/api/users/{userId}", userId)
            .header("Deokhugam-Request-User-ID", UUID.randomUUID().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("닉네임 수정 실패 - 닉네임 미입력 - 400 반환")
  void update_blankNickname_returns400() throws Exception {
    UserUpdateRequest request = new UserUpdateRequest("");

    mockMvc.perform(patch("/api/users/{userId}", userId)
            .header("Deokhugam-Request-User-ID", userId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  // ===== softDelete =====

  @Test
  @DisplayName("논리 삭제 성공 - 204 반환")
  void softDelete_success() throws Exception {
    mockMvc.perform(delete("/api/users/{userId}", userId)
            .header("Deokhugam-Request-User-ID", userId.toString()))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("논리 삭제 실패 - 권한 없음 - 403 반환")
  void softDelete_unauthorized_returns403() throws Exception {
    willThrow(new UnauthorizedAccessException())
        .given(userService).softDelete(any(), any());

    mockMvc.perform(delete("/api/users/{userId}", userId)
            .header("Deokhugam-Request-User-ID", UUID.randomUUID().toString()))
        .andExpect(status().isForbidden());
  }

  // ===== hardDelete =====

  @Test
  @DisplayName("물리 삭제 성공 - 204 반환")
  void hardDelete_success() throws Exception {
    mockMvc.perform(delete("/api/users/{userId}/hard", userId)
            //물리 삭제에도 헤더가 필요해서 헤더 추가
            .header("Deokhugam-Request-User-ID", userId.toString()))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("물리 삭제 실패 - 존재하지 않는 사용자 - 404 반환")
  void hardDelete_notFound_returns404() throws Exception {
    willThrow(UserNotFoundException.withId(userId))
        .given(userService).hardDelete(any());

    mockMvc.perform(delete("/api/users/{userId}/hard", userId)
            //물리 삭제에도 헤더가 필요해서 헤더 추가
            .header("Deokhugam-Request-User-ID", userId.toString()))
        .andExpect(status().isNotFound());
  }

  //"헤더(이름표)가 없으면 통과 못해야 하니까
  @Test
  @DisplayName("닉네임 수정 실패 - 필수 헤더(Deokhugam-Request-User-ID) 누락 - 400 반환")
  void update_missingHeader_returns400() throws Exception {
    //바꾸고 싶다는 닉네임 요청 정보를 만들고
    UserUpdateRequest request = new UserUpdateRequest("새닉네임");

    //가짜로 닉네임 수정 api를 호출한다
    mockMvc.perform(patch("/api/users/{userId}", userId)
            //원래 여기에 .header(..)가 있어야 하지만 실패를 보기 위해 의도적으로 안 넣음
            //그리고 헤더(이름표)없이 문을 두드림
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        //서버가 왜 헤더를 안 가져왔냐고 물으면서 400에러를 응답하는지 확인함
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("닉네임 수정 실패 - 유효하지 않은 UUID 형식 - 400 반환")
  void update_invalidUuidFormat_returns400() throws Exception {
    mockMvc.perform(patch("/api/users/{userId}", userId)
            .header("Deokhugam-Request-User-ID", "invalid-uuid-string") // uuid가 아닌 문자열로 잘못된 형식을 보냄
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new UserUpdateRequest("새닉네임"))))
        .andExpect(status().isBadRequest());
  }

}
