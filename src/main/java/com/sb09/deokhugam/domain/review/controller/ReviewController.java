package com.sb09.deokhugam.domain.review.controller;

import com.sb09.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sb09.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.sb09.deokhugam.domain.review.service.basic.BasicReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

  private final BasicReviewService reviewService;

  /**
   * 1. 리뷰 등록 API [POST] /api/reviews
   */
  @PostMapping
  public ResponseEntity<Void> createReview(
      @RequestHeader("X-User-Id") UUID userId, // 헤더에서 유저 ID 추출
      @Valid @RequestBody ReviewCreateRequest request) {

    reviewService.createReview(request, userId);

    // 성공 시 201 Created 응답
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  /**
   * 2. 리뷰 수정 API [PATCH] /api/reviews/{reviewId}
   */
  @PatchMapping("/{reviewId}")
  public ResponseEntity<Void> updateReview(
      @PathVariable UUID reviewId, // 주소에서 리뷰 ID 추출
      @RequestHeader("X-User-Id") UUID userId,
      @Valid @RequestBody ReviewUpdateRequest request) {

    reviewService.updateReview(reviewId, request, userId);

    // 성공 시 200 OK 응답
    return ResponseEntity.ok().build();
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
}