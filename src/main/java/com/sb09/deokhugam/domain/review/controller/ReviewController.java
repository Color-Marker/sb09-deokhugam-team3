package com.sb09.deokhugam.domain.review.controller;

import com.sb09.deokhugam.domain.review.controller.api.ReviewApi;
import com.sb09.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sb09.deokhugam.domain.review.dto.request.ReviewListRequest;
import com.sb09.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.sb09.deokhugam.domain.review.dto.response.ReviewDto;
import com.sb09.deokhugam.domain.review.service.ReviewService;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController implements ReviewApi {

  private final ReviewService reviewService;

  /**
   * 1. 리뷰 등록 API [POST] /api/reviews
   */
  @PostMapping
  public ResponseEntity<ReviewDto> createReview( // 반환 타입 변경
      @RequestHeader("X-User-Id") UUID userId, // 헤더에서 유저 ID 추출
      @Valid @RequestBody ReviewCreateRequest request) {

    ReviewDto response = reviewService.createReview(request, userId); // Dto 받아오기

    // 성공 시 201 Created 응답
    return ResponseEntity.status(HttpStatus.CREATED).body(response); // body 추가
  }

  /**
   * 2. 리뷰 수정 API [PATCH] /api/reviews/{reviewId}
   */
  @PatchMapping("/{reviewId}")
  public ResponseEntity<ReviewDto> updateReview( // 반환 타입 변경
      @PathVariable UUID reviewId, // 주소에서 리뷰 ID 추출
      @RequestHeader("X-User-Id") UUID userId,
      @Valid @RequestBody ReviewUpdateRequest request) {

    ReviewDto response = reviewService.updateReview(reviewId, request, userId); // Dto 받아오기

    // 성공 시 200 OK 응답
    return ResponseEntity.ok(response); // body 추가
  }

  /**
   * 3. 리뷰 삭제 API (논리 삭제) [DELETE] /api/reviews/{reviewId}
   */
  @DeleteMapping("/{reviewId}")
  public ResponseEntity<Void> deleteReview(
      @PathVariable UUID reviewId,
      @RequestHeader("X-User-Id") UUID userId) {

    reviewService.deleteReview(reviewId, userId);

    // 성공 시 204 No Content 응답
    return ResponseEntity.noContent().build();
  }

  /**
   * 4. 리뷰 목록 조회 API (무한 스크롤 및 검색)
   */
  @GetMapping
  public ResponseEntity<CursorPageResponseDto<ReviewDto>> getReviews(
      @ModelAttribute ReviewListRequest request,
      // 헤더에서 유저 ID를 가져옵니다. (로그인 안 한 경우도 고려해 false 설정)
      @RequestHeader(value = "Deokhugam-Request-User-ID", required = false) UUID userId
  ) {

    CursorPageResponseDto<ReviewDto> response = reviewService.getReviews(request, userId);

    return ResponseEntity.ok(response);
  }
}