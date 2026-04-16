package com.sb09.deokhugam.domain.user.entity;


import com.sb09.deokhugam.global.common.entity.BaseFullAuditEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Users extends BaseFullAuditEntity {

  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @Column(nullable = false, length = 20)
  private String nickname;

  @Column(nullable = false, length = 255)
  private String password;
  
  @Builder
  public Users(String email, String nickname, String password) {
    this.email = email;
    this.nickname = nickname;
    this.password = password;
  }

  // 닉네임 수정 편의 메서드
  public void updateNickname(String nickname) {
    this.nickname = nickname;
  }
}