package com.sb09.deokhugam.domain.review.service;

import com.sb09.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sb09.deokhugam.domain.review.dto.request.ReviewListRequest;
import com.sb09.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.sb09.deokhugam.domain.review.dto.response.ReviewDto;
import com.sb09.deokhugam.domain.review.dto.response.ReviewLikeDto;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import java.util.UUID;
import java.util.List;

public interface ReviewService {

  // 리뷰 등록
  ReviewDto createReview(ReviewCreateRequest request);

  // 리뷰 수정
  ReviewDto updateReview(UUID reviewId, ReviewUpdateRequest request, UUID userId);

  // 리뷰 논리 삭제
  void deleteReview(UUID reviewId, UUID userId);

  // 리뷰 목록 조회
  CursorPageResponseDto<ReviewDto> getReviews(ReviewListRequest request, UUID requestUserId);

  // 리뷰 좋아요 토글
  ReviewLikeDto toggleLike(UUID reviewId, UUID userId);

  // 인기 리뷰 조회
  List<ReviewDto> getPopularReviews();

  // 리뷰 상세 조회
  ReviewDto getReviewDetail(UUID reviewId, UUID currentUserId);

  // 리뷰 물리 삭제
  void hardDeleteReview(UUID reviewId, UUID userId);
}