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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sb09.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sb09.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.sb09.deokhugam.domain.review.dto.response.ReviewDto;
import com.sb09.deokhugam.domain.review.dto.response.ReviewLikeDto;
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

  private static final String USER_ID_HEADER = "Deokhugam-Request-User-ID";

  @Test
  @DisplayName("리뷰 등록 API - 성공 시 201 Created 반환")
  void createReview_success() {
    UUID userId = UUID.randomUUID();
    UUID bookId = UUID.randomUUID();
    ReviewCreateRequest request = new ReviewCreateRequest(userId, bookId, "내용", 5);

    given(reviewService.createReview(any(ReviewCreateRequest.class))).willReturn(null);

    try {
      mockMvc.perform(post("/api/reviews")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @DisplayName("리뷰 수정 API - 성공 시 200 OK 반환")
  void updateReview_success() {
    UUID userId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();
    ReviewUpdateRequest request = new ReviewUpdateRequest("수정", 4);

    given(reviewService.updateReview(eq(reviewId), any(ReviewUpdateRequest.class),
        eq(userId))).willReturn(null);

    try {
      mockMvc.perform(patch("/api/reviews/{reviewId}", reviewId)
              .header(USER_ID_HEADER, userId.toString())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @DisplayName("리뷰 삭제 API - 성공 시 204 No Content 반환")
  void deleteReview_success() {
    UUID userId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();

    try {
      mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId)
              .header(USER_ID_HEADER, userId.toString()))
          .andExpect(status().isNoContent());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @DisplayName("리뷰 목록 조회 API - 성공 시 200 OK 반환")
  void getReviews_success() {
    UUID userId = UUID.randomUUID();
    CursorPageResponseDto<ReviewDto> mockResponse = new CursorPageResponseDto<>(
        List.of(), null, null, 10, 0L, false
    );

    given(reviewService.getReviews(any(), eq(userId))).willReturn(mockResponse);

    try {
      mockMvc.perform(get("/api/reviews")
              .param("orderBy", "LATEST")
              .param("limit", "10")
              .param("requestUserId", userId.toString())
              .header(USER_ID_HEADER, userId.toString())
              .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content").isArray());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @DisplayName("리뷰 좋아요 토글 API - 성공 시 200 OK 및 좋아요 상태 반환")
  void toggleLike_success() {
    UUID userId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();

    ReviewLikeDto mockResponse = new ReviewLikeDto(true, 1);
    given(reviewService.toggleLike(eq(reviewId), eq(userId))).willReturn(mockResponse);

    try {
      mockMvc.perform(post("/api/reviews/{reviewId}/like", reviewId)
              .header(USER_ID_HEADER, userId.toString())
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.liked").value(true));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @DisplayName("인기 리뷰 목록 조회 API - 성공 시 200 OK 및 리스트 반환")
  void getPopularReviews_success() {
    given(reviewService.getPopularReviews()).willReturn(List.of());

    try {
      mockMvc.perform(get("/api/reviews/popular")
              .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isArray());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @DisplayName("리뷰 상세 조회 API - 성공 시 200 OK 반환")
  void getReviewDetail_success() {
    UUID reviewId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    given(reviewService.getReviewDetail(eq(reviewId), eq(userId))).willReturn(null);

    try {
      mockMvc.perform(get("/api/reviews/{reviewId}", reviewId)
              .header(USER_ID_HEADER, userId.toString())
              .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @DisplayName("리뷰 물리 삭제 (하드 삭제) API - 성공 시 204 No Content 반환")
  void hardDeleteReview_success() {
    UUID reviewId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    try {
      mockMvc.perform(delete("/api/reviews/{reviewId}/hard", reviewId)
              .header(USER_ID_HEADER, userId.toString()))
          .andExpect(status().isNoContent());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}