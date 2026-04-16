package com.sb09.deokhugam.domain.review.service;

import com.sb09.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sb09.deokhugam.domain.review.dto.request.ReviewUpdateRequest;

import java.util.UUID;

public interface ReviewService {

  // 리뷰 등록
  void createReview(ReviewCreateRequest request, UUID userId);

  // 리뷰 수정
  void updateReview(UUID reviewId, ReviewUpdateRequest request, UUID userId);

  // 리뷰 삭제 (논리 삭제)
  void deleteReview(UUID reviewId, UUID userId);
}