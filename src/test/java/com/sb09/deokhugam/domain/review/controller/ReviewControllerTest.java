package com.sb09.deokhugam.domain.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sb09.deokhugam.domain.review.dto.response.ReviewLikeDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sb09.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sb09.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.sb09.deokhugam.domain.review.dto.response.ReviewDto;
import com.sb09.deokhugam.domain.review.service.ReviewService;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ReviewService reviewService;

  @Test
  @DisplayName("리뷰 등록 API - 성공 시 201 Created 반환")
  void createReview_success() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    UUID bookId = UUID.randomUUID();
    ReviewCreateRequest request = new ReviewCreateRequest(bookId, "너무 재밌는 책!", 5);

    given(reviewService.createReview(any(), any())).willReturn(null);

    // when & then
    mockMvc.perform(post("/api/reviews")
            .header("X-User-Id", userId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
  }

  @Test
  @DisplayName("리뷰 수정 API - 성공 시 200 OK 반환")
  void updateReview_success() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();
    ReviewUpdateRequest request = new ReviewUpdateRequest("내용 수정합니다", 4);

    // doNothing() 대신 Dto(여기선 null)를 반환
    given(reviewService.updateReview(eq(reviewId), any(), eq(userId))).willReturn(null);

    // when & then
    mockMvc.perform(patch("/api/reviews/{reviewId}", reviewId)
            .header("X-User-Id", userId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("리뷰 삭제 API - 성공 시 204 No Content 반환")
  void deleteReview_success() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();

    // when & then
    mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId)
            .header("X-User-Id", userId.toString()))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("리뷰 목록 조회 API - 성공 시 200 OK 반환")
  void getReviews_success() throws Exception {
    // given
    UUID userId = UUID.randomUUID();

    CursorPageResponseDto<ReviewDto> mockResponse = new CursorPageResponseDto<>(
        List.of(), null, null, 10, 0L, false
    );

    given(reviewService.getReviews(any(), eq(userId))).willReturn(mockResponse);

    // when & then
    mockMvc.perform(get("/api/reviews")
            .param("limit", "10")
            .header("Deokhugam-Request-User-ID", userId.toString())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  @DisplayName("리뷰 좋아요 토글 API - 성공 시 200 OK 및 좋아요 상태 반환")
  void toggleLike_success() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();

    // 서비스가 true와 1을 반환한다고 (Mock)설정
    ReviewLikeDto mockResponse = new ReviewLikeDto(true, 1);
    given(reviewService.toggleLike(eq(reviewId), eq(userId))).willReturn(mockResponse);

    // when & then
    mockMvc.perform(post("/api/reviews/{reviewId}/likes", reviewId)
            .header("X-User-Id", userId.toString()) // 헤더에 유저 ID 주입
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()) // 200 OK 상태 코드 검증
        .andExpect(jsonPath("$.liked").value(true)) // JSON 응답 알맹이 검증
        .andExpect(jsonPath("$.likeCount").value(1));
  }
}