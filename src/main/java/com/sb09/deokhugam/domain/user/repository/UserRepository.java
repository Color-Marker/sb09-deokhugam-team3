package com.sb09.deokhugam.domain.user.repository;

import com.sb09.deokhugam.domain.user.entity.Users;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, UUID> {

  // 회원가입: 논리 삭제되지 않은 사용자 기준 이메일 중복 검증
  boolean existsByEmailAndDeletedAtIsNull(String email);

  // 로그인: 논리 삭제되지 않은 사용자 이메일 조회
  Optional<Users> findByEmailAndDeletedAtIsNull(String email);

  // 단건 조회: 논리 삭제되지 않은 사용자 ID 조회
  Optional<Users> findByIdAndDeletedAtIsNull(UUID id);

  // 스케줄러: 논리 삭제(deleted_at) 마킹 후 특정 시간이 지난 유저 목록 조회
  List<Users> findAllByDeletedAtBefore(LocalDateTime threshold);
}
