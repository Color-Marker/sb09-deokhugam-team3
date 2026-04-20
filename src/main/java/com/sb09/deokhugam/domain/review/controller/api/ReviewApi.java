package com.sb09.deokhugam.domain.review.controller.api;

import com.sb09.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sb09.deokhugam.domain.review.dto.request.ReviewListRequest;
import com.sb09.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.sb09.deokhugam.domain.review.dto.response.ReviewDto;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "리뷰 관리", description = "도서 리뷰 관련 API")
public interface ReviewApi {

  @Operation(summary = "리뷰 등록", description = "새로운 도서 리뷰를 등록합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "리뷰 등록 성공 (생성된 리뷰 정보 반환)"), // 수정됨
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (데이터 형식 오류)"),
      @ApiResponse(responseCode = "404", description = "도서 또는 사용자를 찾을 수 없음"),
      @ApiResponse(responseCode = "409", description = "이미 해당 도서에 리뷰를 작성한 사용자")
  })
  ResponseEntity<ReviewDto> createReview( // 반환 타입 ReviewDto로 변경됨
      @Parameter(description = "인증된 유저 ID") @RequestHeader("X-User-Id") UUID userId,
      @Valid @RequestBody ReviewCreateRequest request
  );

  @Operation(summary = "리뷰 수정", description = "작성한 리뷰의 내용 및 평점을 수정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "리뷰 수정 성공 (수정된 리뷰 정보 반환)"), // 수정됨
      @ApiResponse(responseCode = "403", description = "리뷰 수정 권한 없음 (타인 리뷰)"),
      @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
  })
  ResponseEntity<ReviewDto> updateReview( // 반환 타입 ReviewDto로 변경됨
      @Parameter(description = "리뷰 ID") @PathVariable UUID reviewId,
      @Parameter(description = "인증된 유저 ID") @RequestHeader("X-User-Id") UUID userId,
      @Valid @RequestBody ReviewUpdateRequest request
  );

  @Operation(summary = "리뷰 삭제 (논리 삭제)", description = "작성한 리뷰를 삭제 처리합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "리뷰 삭제 성공"),
      @ApiResponse(responseCode = "403", description = "리뷰 삭제 권한 없음"),
      @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음"),
      @ApiResponse(responseCode = "409", description = "이미 삭제된 리뷰")
  })
  ResponseEntity<Void> deleteReview(
      @Parameter(description = "리뷰 ID") @PathVariable UUID reviewId,
      @Parameter(description = "인증된 유저 ID") @RequestHeader("X-User-Id") UUID userId
  );

  @Operation(summary = "리뷰 목록 조회", description = "무한 스크롤 방식을 적용하여 리뷰 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "리뷰 목록 조회 성공")
  })
  ResponseEntity<CursorPageResponseDto<ReviewDto>> getReviews(
      @ModelAttribute ReviewListRequest request,
      @Parameter(description = "유저 ID (비로그인 시 생략 가능)") @RequestHeader(value = "Deokhugam-Request-User-ID", required = false) UUID userId
  );
}