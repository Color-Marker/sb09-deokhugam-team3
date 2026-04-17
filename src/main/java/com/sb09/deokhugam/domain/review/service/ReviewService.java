package com.sb09.deokhugam.domain.review.service;

import com.sb09.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sb09.deokhugam.domain.review.dto.request.ReviewListRequest;
import com.sb09.deokhugam.domain.review.dto.request.ReviewUpdateRequest;

import com.sb09.deokhugam.domain.review.dto.response.ReviewDto;
import com.sb09.deokhugam.domain.review.entity.Review;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import java.util.UUID;
import org.springframework.data.domain.Slice;

public interface ReviewService {

  // 리뷰 등록
  void createReview(ReviewCreateRequest request, UUID userId);

  // 리뷰 수정
  void updateReview(UUID reviewId, ReviewUpdateRequest request, UUID userId);

  // 리뷰 삭제 (논리 삭제)
  void deleteReview(UUID reviewId, UUID userId);

  CursorPageResponseDto<ReviewDto> getReviews(ReviewListRequest request, UUID currentUserId);
}