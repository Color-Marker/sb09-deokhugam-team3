package com.sb09.deokhugam.domain.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
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

// 🌟 스프링부트를 다 켜지 않고, 웹 계층(Controller)만 쏙 빼서 가볍게 테스트하는 어노테이션
@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

  @Autowired
  private MockMvc mockMvc; // 🌟 가짜 HTTP 요청(Postman 역할)을 쏴주는 마법의 도구

  @Autowired
  private ObjectMapper objectMapper; // 🌟 자바 객체를 JSON 문자열로 바꿔주는 도구

  @MockitoBean // 컨트롤러가 사용할 서비스는 가짜(Mock)로 주입!
  private ReviewService reviewService;

  @Test
  @DisplayName("리뷰 등록 API - 성공 시 201 Created 반환")
  void createReview_success() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    UUID bookId = UUID.randomUUID();
    ReviewCreateRequest request = new ReviewCreateRequest(bookId, "너무 재밌는 책!", 5);

    // 서비스는 아무것도 안 하고 통과(void)한다고 가정
    doNothing().when(reviewService).createReview(any(), any());

    // when & then
    mockMvc.perform(post("/api/reviews")
            .header("X-User-Id", userId.toString()) // 헤더에 유저 ID 세팅
            .contentType(MediaType.APPLICATION_JSON) // 데이터 형식은 JSON
            .content(objectMapper.writeValueAsString(request))) // 객체를 JSON 문자열로 변환해서 바디에 넣음
        .andExpect(status().isCreated()); // 🌟 201 상태 코드가 돌아오는지 확인!
  }

  @Test
  @DisplayName("리뷰 수정 API - 성공 시 200 OK 반환")
  void updateReview_success() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();
    ReviewUpdateRequest request = new ReviewUpdateRequest("내용 수정합니다", 4);

    doNothing().when(reviewService).updateReview(eq(reviewId), any(), eq(userId));

    // when & then
    mockMvc.perform(patch("/api/reviews/{reviewId}", reviewId) // 주소의 {reviewId}에 값 세팅
            .header("X-User-Id", userId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk()); // 🌟 200 상태 코드가 돌아오는지 확인!
  }

  @Test
  @DisplayName("리뷰 삭제 API - 성공 시 204 No Content 반환")
  void deleteReview_success() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();

    doNothing().when(reviewService).deleteReview(reviewId, userId);

    // when & then
    mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId)
            .header("X-User-Id", userId.toString()))
        .andExpect(status().isNoContent()); // 🌟 204 상태 코드가 돌아오는지 확인!
  }

  @Test
  @DisplayName("리뷰 목록 조회 API - 성공 시 200 OK 반환")
  void getReviews_success() throws Exception {
    // given
    UUID userId = UUID.randomUUID();

    // 🌟 복잡한 ReviewDto 생성 과정을 싹 날리고, 빈 배열(List.of())을 넣어서 응답을 흉내냅니다!
    CursorPageResponseDto<ReviewDto> mockResponse = new CursorPageResponseDto<>(
        List.of(), null, null, 10, 0L, false
    );

    // 서비스가 저 빈 상자를 반환하도록 세팅
    given(reviewService.getReviews(any(), eq(userId))).willReturn(mockResponse);

    // when & then
    mockMvc.perform(get("/api/reviews")
            .param("limit", "10") // 쿼리 파라미터 세팅
            .header("Deokhugam-Request-User-ID", userId.toString())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()) // 🌟 200 OK가 떨어지는지 확인
        .andExpect(jsonPath("$.content").isArray()); // 🌟 content가 배열 형태로 잘 나오는지 확인!
  }
}