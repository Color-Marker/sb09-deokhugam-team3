package com.sb09.deokhugam.domain.review.controller;

import com.sb09.deokhugam.domain.review.controller.api.ReviewApi;
import com.sb09.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sb09.deokhugam.domain.review.dto.request.ReviewListRequest;
import com.sb09.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.sb09.deokhugam.domain.review.dto.response.PopularReviewDto;
import com.sb09.deokhugam.domain.review.dto.response.ReviewDto;
import com.sb09.deokhugam.domain.review.dto.response.ReviewLikeDto;
import com.sb09.deokhugam.domain.review.service.ReviewService;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
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
   * 1. 리뷰 등록
   */
  @Override
  @PostMapping
  public ResponseEntity<ReviewDto> createReview(@Valid @RequestBody ReviewCreateRequest request) {
    // Body에 포함된 userId를 사용하여 리뷰를 생성합니다.
    ReviewDto response = reviewService.createReview(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * 2. 리뷰 수정
   */
  @Override
  @PatchMapping("/{reviewId}")
  public ResponseEntity<ReviewDto> updateReview(
      @PathVariable UUID reviewId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID userId,
      @Valid @RequestBody ReviewUpdateRequest request) {

    ReviewDto response = reviewService.updateReview(reviewId, request, userId);
    return ResponseEntity.ok(response);
  }

  /**
   * 3. 리뷰 논리 삭제
   */
  @Override
  @DeleteMapping("/{reviewId}")
  public ResponseEntity<Void> deleteReview(
      @PathVariable UUID reviewId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID userId) {

    reviewService.deleteReview(reviewId, userId);
    return ResponseEntity.noContent().build();
  }

  /**
   * 4. 리뷰 목록 조회
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
      @RequestParam(defaultValue = "LATEST") String orderBy,
      @RequestParam(defaultValue = "DESC") Sort.Direction direction,
      @RequestParam(required = false) UUID requestUserId,
      @RequestHeader(value = "Deokhugam-Request-User-ID", required = false) UUID headerUserId
  ) {

    // ReviewListRequest 내부에서도 direction을 처리할 수 있도록 생성자 확인이 필요합니다.
    ReviewListRequest request = new ReviewListRequest(
        bookId, userId, keyword, limit, cursor, after, orderBy, direction
    );

    CursorPageResponseDto<ReviewDto> response = reviewService.getReviews(request, requestUserId);
    return ResponseEntity.ok(response);
  }

  /**
   * 5. 리뷰 좋아요 토글
   */
  @Override
  @PostMapping("/{reviewId}/like")
  public ResponseEntity<ReviewLikeDto> toggleLike(
      @PathVariable UUID reviewId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID userId) {

    ReviewLikeDto response = reviewService.toggleLike(reviewId, userId);
    return ResponseEntity.ok(response);
  }

  /**
   * 6. 인기 리뷰 목록 조회
   */
  @Override
  @GetMapping("/popular")
  public ResponseEntity<?> getPopularReviews(
      @RequestParam(defaultValue = "ALL") String period
  ) {
    // 서비스 호출할 때 period 값을 넘겨주도록 수정
    List<PopularReviewDto> popularReviews = reviewService.getPopularReviews(period);

    // 프론트엔드 맞춰 "content"라는 구조로 반환
    return ResponseEntity.ok(java.util.Map.of("content", popularReviews));
  }

  /**
   * 7. 리뷰 상세 조회
   */
  @Override
  @GetMapping("/{reviewId}")
  public ResponseEntity<ReviewDto> getReviewDetail(
      @PathVariable UUID reviewId,
      @RequestHeader(value = "Deokhugam-Request-User-ID", required = false) UUID userId) {

    return ResponseEntity.ok(reviewService.getReviewDetail(reviewId, userId));
  }

  /**
   * 8. 리뷰 물리 삭제 (하드 삭제)
   */
  @Override
  @DeleteMapping("/{reviewId}/hard")
  public ResponseEntity<Void> hardDeleteReview(
      @PathVariable UUID reviewId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID userId) {

    reviewService.hardDeleteReview(reviewId, userId);
    return ResponseEntity.noContent().build();
  }
}