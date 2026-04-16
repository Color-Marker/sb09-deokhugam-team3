package com.sb09.deokhugam.domain.review.repository;

import com.sb09.deokhugam.domain.review.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, UUID> {

  // 특정 리뷰에 특정 유저가 좋아요를 눌렀는지 확인하기 위한 메서드
  Optional<ReviewLike> findByReviewIdAndUserId(UUID reviewId, UUID userId);

  // 특정 리뷰에 특정 유저가 좋아요를 누른 기록이 존재하는지 여부 확인
  boolean existsByReviewIdAndUserId(UUID reviewId, UUID userId);
}