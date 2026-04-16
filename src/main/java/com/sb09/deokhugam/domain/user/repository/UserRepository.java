package com.sb09.deokhugam.domain.user.repository;

import com.sb09.deokhugam.domain.user.entity.Users;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, UUID> {

  // 이메일 중복 확인
  boolean existsByEmail(String email);

  // 이메일로 유저 찾기 (로그인 등에서 사용)
  Optional<Users> findByEmail(String email);
}

