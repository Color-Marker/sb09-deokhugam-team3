package com.sb09.deokhugam.domain.review.repository;

import com.sb09.deokhugam.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

  // 리뷰 중복 검사
  boolean existsByBookIdAndUserId(UUID bookId, UUID userId);

}