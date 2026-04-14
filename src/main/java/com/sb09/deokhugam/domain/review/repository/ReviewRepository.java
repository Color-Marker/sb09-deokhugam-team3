package com.sb09.deokhugam.domain.review.repository;

import com.sb09.deokhugam.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

  // 리뷰 등록 전, 해당 유저가 이 책에 이미 리뷰를 썼는지 검증하기 위한 메서드
  boolean existsByBookIdAndUserId(UUID bookId, UUID userId);

}