package com.sb09.deokhugam.domain.user.dto.Response;

import com.sb09.deokhugam.domain.user.entity.Users;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(

    UUID id,
    String email,
    String nickname,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt
) {

  // Users 엔티티를 UserResponse DTO로 변환하는 편의 메서드
  public static UserResponse from(Users user) {
    return new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getNickname(),
        user.getCreatedAt()
    );
  }
}
