package com.sb09.deokhugam.domain.review.service;

import com.sb09.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sb09.deokhugam.domain.review.dto.request.ReviewListRequest;
import com.sb09.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.sb09.deokhugam.domain.review.dto.response.ReviewDto;
import com.sb09.deokhugam.domain.review.dto.response.ReviewLikeDto;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import java.util.UUID;

public interface ReviewService {

  // 리뷰 등록
  ReviewDto createReview(ReviewCreateRequest request, UUID userId);

  // 리뷰 수정
  ReviewDto updateReview(UUID reviewId, ReviewUpdateRequest request, UUID userId);

  // 리뷰 삭제 (논리 삭제) - 삭제는 돌려줄 데이터가 없으니 그대로 void
  void deleteReview(UUID reviewId, UUID userId);

  // 리뷰 목록 조회
  CursorPageResponseDto<ReviewDto> getReviews(ReviewListRequest request, UUID currentUserId);

  // 리뷰 좋아요 토글
  ReviewLikeDto toggleLike(UUID reviewId, UUID userId);
}