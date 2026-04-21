package com.sb09.deokhugam.domain.review.controller;

import com.sb09.deokhugam.domain.review.controller.api.ReviewApi;
import com.sb09.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sb09.deokhugam.domain.review.dto.request.ReviewListRequest;
import com.sb09.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.sb09.deokhugam.domain.review.dto.response.ReviewDto;
import com.sb09.deokhugam.domain.review.dto.response.ReviewLikeDto;
import com.sb09.deokhugam.domain.review.service.ReviewService;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController implements ReviewApi {

  private final ReviewService reviewService;

  /**
   * 1. 리뷰 등록 API [POST] /api/reviews
   */
  @Override
  @PostMapping
  public ResponseEntity<ReviewDto> createReview(
      @RequestHeader("X-User-Id") UUID userId,
      @Valid @RequestBody ReviewCreateRequest request) {

    ReviewDto response = reviewService.createReview(request, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * 2. 리뷰 수정 API [PATCH] /api/reviews/{reviewId}
   */
  @Override
  @PatchMapping("/{reviewId}")
  public ResponseEntity<ReviewDto> updateReview(
      @PathVariable UUID reviewId,
      @RequestHeader("X-User-Id") UUID userId,
      @Valid @RequestBody ReviewUpdateRequest request) {

    ReviewDto response = reviewService.updateReview(reviewId, request, userId);
    return ResponseEntity.ok(response);
  }

  /**
   * 3. 리뷰 삭제 API (논리 삭제) [DELETE] /api/reviews/{reviewId}
   */
  @Override
  @DeleteMapping("/{reviewId}")
  public ResponseEntity<Void> deleteReview(
      @PathVariable UUID reviewId,
      @RequestHeader("X-User-Id") UUID userId) {

    reviewService.deleteReview(reviewId, userId);
    return ResponseEntity.noContent().build();
  }

  /**
   * 4. 리뷰 목록 조회 API (무한 스크롤 및 검색)
   */
  @Override
  @GetMapping
  public ResponseEntity<CursorPageResponseDto<ReviewDto>> getReviews(
      @RequestParam(required = false) UUID bookId,
      @RequestParam(required = false) UUID userId,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "10") int limit,
      @RequestParam(required = false) UUID cursor,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after,
      @RequestParam(defaultValue = "LATEST") String sortBy,
      // rating 파라미터는 삭제됨
      @RequestHeader(value = "Deokhugam-Request-User-ID", required = false) UUID requestUserId
  ) {

    ReviewListRequest request = new ReviewListRequest(
        bookId, userId, keyword, limit, cursor, after, sortBy, null
    );

    CursorPageResponseDto<ReviewDto> response = reviewService.getReviews(request, requestUserId);
    return ResponseEntity.ok(response);
  }

  /**
   * 5. 리뷰 좋아요 토글 (추가/취소) API [POST] /api/reviews/{reviewId}/likes
   */
  @Override
  @PostMapping("/{reviewId}/likes")
  public ResponseEntity<ReviewLikeDto> toggleLike(
      @PathVariable UUID reviewId,
      @RequestHeader("X-User-Id") UUID userId) {

    ReviewLikeDto response = reviewService.toggleLike(reviewId, userId);
    return ResponseEntity.ok(response);
  }

  /**
   * 6. 인기 리뷰 목록 조회 API [GET] /api/reviews/popular
   */
  @Override
  @GetMapping("/popular")
  public ResponseEntity<List<ReviewDto>> getPopularReviews() {
    return ResponseEntity.ok(reviewService.getPopularReviews());
  }

  /**
   * 7. 리뷰 상세 조회 API [GET] /api/reviews/{reviewId}
   */
  @Override
  @GetMapping("/{reviewId}")
  public ResponseEntity<ReviewDto> getReviewDetail(
      @PathVariable UUID reviewId,
      @RequestHeader(value = "X-User-Id", required = false) UUID userId) {

    return ResponseEntity.ok(reviewService.getReviewDetail(reviewId, userId));
  }

  /**
   * 8. 리뷰 물리 삭제 (하드 삭제) [DELETE] /api/reviews/{reviewId}/hard
   */
  @Override
  @DeleteMapping("/{reviewId}/hard")
  public ResponseEntity<Void> hardDeleteReview(
      @PathVariable UUID reviewId) {

    reviewService.hardDeleteReview(reviewId);
    return ResponseEntity.noContent().build();
  }
}