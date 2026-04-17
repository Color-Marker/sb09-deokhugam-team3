package com.sb09.deokhugam.domain.user.controller;

import com.sb09.deokhugam.domain.user.controller.api.UserApi;
import com.sb09.deokhugam.domain.user.dto.Response.UserResponse;
import com.sb09.deokhugam.domain.user.dto.request.UserLoginRequest;
import com.sb09.deokhugam.domain.user.dto.request.UserRegisterRequest;
import com.sb09.deokhugam.domain.user.dto.request.UserUpdateRequest;
import com.sb09.deokhugam.domain.user.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<UserResponse> register(
      @Valid @RequestBody UserRegisterRequest request
  ) {
    UserResponse response = userService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<UserResponse> login(
      @Valid @RequestBody UserLoginRequest request
  ) {
    UserResponse response = userService.login(request);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{userId}")
  public ResponseEntity<UserResponse> getUser(
      @PathVariable UUID userId
  ) {
    UserResponse response = userService.getUser(userId);
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{userId}")
  public ResponseEntity<UserResponse> updateUser(
      @PathVariable UUID userId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId,
      @Valid @RequestBody UserUpdateRequest request
  ) {
    UserResponse response = userService.updateNickname(requestUserId, userId, request);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> deleteUser(
      @PathVariable UUID userId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
  ) {
    userService.softDelete(requestUserId, userId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{userId}/hard")
  public ResponseEntity<Void> hardDeleteUser(
      @PathVariable UUID userId
  ) {
    userService.hardDelete(userId);
    return ResponseEntity.noContent().build();
  }
}
