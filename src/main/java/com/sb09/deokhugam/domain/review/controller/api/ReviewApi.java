package com.sb09.deokhugam.domain.review.controller.api;

import com.sb09.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sb09.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.sb09.deokhugam.domain.review.dto.response.ReviewDto;
import com.sb09.deokhugam.domain.review.dto.response.ReviewLikeDto;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "리뷰 관리", description = "도서 리뷰 관련 API")
public interface ReviewApi {

  @Operation(summary = "리뷰 등록", description = "사용자 ID를 포함한 리뷰 정보를 등록합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "리뷰 등록 성공"),
      @ApiResponse(responseCode = "404", description = "도서 또는 사용자를 찾을 수 없음"),
      @ApiResponse(responseCode = "409", description = "이미 리뷰를 작성함")
  })
  @PostMapping
  ResponseEntity<ReviewDto> createReview(
      @Valid @RequestBody ReviewCreateRequest request
  );

  @Operation(summary = "리뷰 수정", description = "작성한 리뷰를 수정합니다.")
  @PatchMapping("/{reviewId}")
  ResponseEntity<ReviewDto> updateReview(
      @Parameter(description = "리뷰 ID") @PathVariable UUID reviewId,
      @Parameter(description = "인증된 유저 ID") @RequestHeader("Deokhugam-Request-User-ID") UUID userId,
      @Valid @RequestBody ReviewUpdateRequest request
  );

  @Operation(summary = "리뷰 논리 삭제", description = "리뷰를 논리적으로 삭제 처리합니다.")
  @DeleteMapping("/{reviewId}")
  ResponseEntity<Void> deleteReview(
      @Parameter(description = "리뷰 ID") @PathVariable UUID reviewId,
      @Parameter(description = "인증된 유저 ID") @RequestHeader("Deokhugam-Request-User-ID") UUID userId
  );

  @Operation(summary = "리뷰 목록 조회", description = "무한 스크롤 방식을 적용하여 리뷰 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "리뷰 목록 조회 성공")
  })
  @GetMapping
  ResponseEntity<CursorPageResponseDto<ReviewDto>> getReviews(
      @Parameter(description = "특정 도서의 리뷰만 볼 때") @RequestParam(required = false) UUID bookId,
      @Parameter(description = "특정 유저가 쓴 리뷰만 볼 때") @RequestParam(required = false) UUID userId,
      @Parameter(description = "검색어 (내용/도서명/닉네임)") @RequestParam(required = false) String keyword,
      @Parameter(description = "가져올 데이터 개수") @RequestParam(defaultValue = "10") int limit,
      @Parameter(description = "커서: 마지막으로 본 리뷰 ID") @RequestParam(required = false) UUID cursor,
      @Parameter(description = "커서: 마지막으로 본 리뷰의 생성일시")
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after,
      @Parameter(description = "정렬 기준 (LATEST 또는 RATING)") @RequestParam(defaultValue = "LATEST") String orderBy,
      @Parameter(description = "정렬 방향 (ASC 또는 DESC)") @RequestParam(defaultValue = "DESC") Sort.Direction direction,
      @Parameter(description = "요청자 ID (좋아요 확인용 쿼리 파라미터)") @RequestParam(required = false) UUID requestUserId,
      @Parameter(description = "인증 헤더") @RequestHeader(value = "Deokhugam-Request-User-ID", required = false) UUID headerUserId
  );

  @Operation(summary = "리뷰 좋아요", description = "좋아요를 추가하거나 취소합니다.")
  @PostMapping("/{reviewId}/like")
  ResponseEntity<ReviewLikeDto> toggleLike(
      @Parameter(description = "리뷰 ID") @PathVariable UUID reviewId,
      @Parameter(description = "인증된 유저 ID") @RequestHeader("Deokhugam-Request-User-ID") UUID userId
  );

  @Operation(summary = "인기 리뷰 목록 조회", description = "좋아요가 많은 순으로 상위 리뷰를 조회합니다.")
  @GetMapping("/popular")
  ResponseEntity<?> getPopularReviews(
      @Parameter(description = "조회 기간 (DAILY, WEEKLY, MONTHLY, ALL)") @RequestParam(defaultValue = "ALL") String period
  );

  @Operation(summary = "리뷰 상세 조회", description = "리뷰 ID로 상세 내용을 조회합니다.")
  @GetMapping("/{reviewId}")
  ResponseEntity<ReviewDto> getReviewDetail(
      @Parameter(description = "리뷰 ID") @PathVariable UUID reviewId,
      @Parameter(description = "유저 ID (좋아요 확인용)") @RequestHeader(value = "Deokhugam-Request-User-ID", required = false) UUID userId
  );

  @Operation(summary = "리뷰 물리 삭제", description = "DB에서 리뷰를 완전히 삭제합니다.")
  @DeleteMapping("/{reviewId}/hard")
  ResponseEntity<Void> hardDeleteReview(
      @Parameter(description = "리뷰 ID") @PathVariable UUID reviewId,
      @Parameter(description = "인증된 유저 ID") @RequestHeader("Deokhugam-Request-User-ID") UUID userId
  );
}