package com.sb09.deokhugam.domain.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sb09.deokhugam.domain.comment.dto.CommentDto;
import com.sb09.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.sb09.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.sb09.deokhugam.domain.comment.service.CommentService;
import com.sb09.deokhugam.config.RequestTrackingFilter;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import com.sb09.deokhugam.global.exception.CustomException;
import com.sb09.deokhugam.global.exception.ErrorCode;
import com.sb09.deokhugam.global.exception.comment.CommentNotFoundException;
import com.sb09.deokhugam.global.exception.comment.ForbiddenAuthorityException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    value = CommentController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = RequestTrackingFilter.class
    )
)
class CommentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private CommentService commentService;

  private UUID commentId;
  private UUID reviewId;
  private UUID userId;
  private CommentDto commentDto;

  @BeforeEach
  void setUp() {
    commentId = UUID.randomUUID();
    reviewId = UUID.randomUUID();
    userId = UUID.randomUUID();

    commentDto = new CommentDto(
        commentId,
        reviewId,
        userId,
        "테스트유저",
        "테스트 댓글 내용",
        LocalDateTime.now(),
        LocalDateTime.now()
    );
  }

  @Test
  @DisplayName("댓글 생성 성공 - 201 반환")
  void createComment_success() throws Exception {
    CommentCreateRequest request = new CommentCreateRequest(reviewId, userId, "테스트 댓글 내용");
    given(commentService.create(any())).willReturn(commentDto);

    mockMvc.perform(post("/api/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.content").value("테스트 댓글 내용"));
  }

  @Test
  @DisplayName("댓글 생성 실패 - content 없으면 400 반환")
  void createComment_noContent_returns400() throws Exception {
    CommentCreateRequest request = new CommentCreateRequest(reviewId, userId, "");

    mockMvc.perform(post("/api/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("댓글 생성 실패 - reviewId 누락 - 400")
  void createComment_nullReviewId_returns400() throws Exception {
    CommentCreateRequest request = new CommentCreateRequest(null, userId, "테스트 댓글 내용");

    mockMvc.perform(post("/api/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("댓글 생성 - userId 누락 - 400")
  void createComment_nullUserId_returns400() throws Exception {
    CommentCreateRequest request = new CommentCreateRequest(reviewId, null, "테스트 댓글 내용");

    mockMvc.perform(post("/api/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("댓글 생성 실패 - 존재하지 않는 리뷰 - 404")
  void createComment_reviewNotFound_returns404() throws Exception {
    CommentCreateRequest request = new CommentCreateRequest(reviewId, userId, "테스트 댓글 내용");
    given(commentService.create(any()))
        .willThrow(new CustomException(ErrorCode.REVIEW_NOT_FOUND));

    mockMvc.perform(post("/api/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("댓글 생성 실패 - content size초과 - 400")
  void createComment_tooLongContent_returns400() throws Exception {
    CommentCreateRequest request = new CommentCreateRequest(reviewId, userId, "a".repeat(501));

    mockMvc.perform(post("/api/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("댓글 단건 조회 성공 - 200 반환")
  void getComment_success() throws Exception {
    given(commentService.findById(commentId)).willReturn(commentDto);

    mockMvc.perform(get("/api/comments/{commentId}", commentId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(commentId.toString()));
  }

  @Test
  @DisplayName("댓글 단건 조회 실패 - 없는 댓글이면 404 반환")
  void getComment_notFound_returns404() throws Exception {
    given(commentService.findById(any()))
        .willThrow(CommentNotFoundException.withId(commentId));

    mockMvc.perform(get("/api/comments/{commentId}", UUID.randomUUID()))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("댓글 단건 조회 실패 - 논리삭제된 댓글 - 410")
  void getComment_alreadyDeleted_returns410() throws Exception {
    given(commentService.findById(any()))
        .willThrow(new CustomException(ErrorCode.DELETED_COMMENT));

    mockMvc.perform(get("/api/comments/{commentId}", commentId))
        .andExpect(status().isGone());
  }

  @Test
  @DisplayName("댓글 목록 조회 성공 - 200")
  void getComments_success() throws Exception {
    CursorPageResponseDto<CommentDto> response = new CursorPageResponseDto<>(
        List.of(commentDto), null, null, 1, 1L, false
    );
    given(commentService.findAllByReviewId(any())).willReturn(response);

    mockMvc.perform(get("/api/comments")
            .param("reviewId", reviewId.toString()))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("댓글 목록 조회 실패 - reviewId 누락 - 400")
  void getComments_nullReviewId_returns400() throws Exception {
    mockMvc.perform(get("/api/comments"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("댓글 목록 조회 실패 - limit 0 - 400")
  void getComments_zeroLimit_returns400() throws Exception {
    mockMvc.perform(get("/api/comments")
            .param("reviewId", reviewId.toString())
            .param("limit", "0"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("댓글 목록 조회 실패 - limit 음수 - 400")
  void getComments_negativeLimit_returns400() throws Exception {
    mockMvc.perform(get("/api/comments")
            .param("reviewId", reviewId.toString())
            .param("limit", "-1"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("댓글 목록 조회 실패 - limit 초과 - 400")
  void getComments_limitOver100_returns400() throws Exception {
    mockMvc.perform(get("/api/comments")
            .param("reviewId", reviewId.toString())
            .param("limit", "101"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("댓글 목록 조회 실패 - 잘못된 정렬 방향 - 400")
  void getComments_invalidDirection_returns400() throws Exception {
    mockMvc.perform(get("/api/comments")
            .param("reviewId", reviewId.toString())
            .param("direction", "invalid"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("댓글 목록 조회 실패 - 존재하지 않는 리뷰 - 404")
  void getComments_reviewNotFound_returns404() throws Exception {
    given(commentService.findAllByReviewId(any()))
        .willThrow(new CustomException(ErrorCode.REVIEW_NOT_FOUND));

    mockMvc.perform(get("/api/comments")
            .param("reviewId", UUID.randomUUID().toString()))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("댓글 목록 조회 실패 - cursor UUID 형식 오류 - 400")
  void getComments_invalidCursor_returns400() throws Exception {
    mockMvc.perform(get("/api/comments")
            .param("reviewId", reviewId.toString())
            .param("cursor", "not-a-uuid"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("댓글 목록 조회 실패 - after 날짜 형식 오류 - 400")
  void getComments_invalidAfter_returns400() throws Exception {
    mockMvc.perform(get("/api/comments")
            .param("reviewId", reviewId.toString())
            .param("after", "not-a-date"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("댓글 수정 성공(DTO) - 200 반환")
  void updateComment_success() throws Exception {
    CommentUpdateRequest request = new CommentUpdateRequest("수정된 내용");
    given(commentService.update(any(), any(), any())).willReturn(commentDto);

    mockMvc.perform(patch("/api/comments/{commentId}", commentId)
            .header("Deokhugam-Request-User-ID", userId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("댓글 수정 실패 - 헤더 누락 - 400")
  void updateComment_missingHeader_returns400() throws Exception {
    CommentUpdateRequest request = new CommentUpdateRequest("수정 내용");
    mockMvc.perform(patch("/api/comments/{commentId}", commentId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("댓글 수정 실패 - size 초과 - 400")
  void updateComment_overSize_returns400() throws Exception {
    CommentUpdateRequest request = new CommentUpdateRequest("a".repeat(501));
    mockMvc.perform(patch("/api/comments/{commentId}", commentId)
            .header("Deokhugam-Request-User-ID", userId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("댓글 수정 실패 - 권한 없으면 403 반환")
  void updateComment_forbidden_returns403() throws Exception {
    CommentUpdateRequest request = new CommentUpdateRequest("수정된 내용");
    given(commentService.update(any(), any(), any()))
        .willThrow(new CustomException(ErrorCode.COMMENT_UPDATE_FORBIDDEN));

    mockMvc.perform(patch("/api/comments/{commentId}", commentId)
            .header("Deokhugam-Request-User-ID", UUID.randomUUID().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("댓글 수정 실패 - 존재하지 않는 댓글 - 404")
  void updateComment_notFound_returns404() throws Exception {
    CommentUpdateRequest request = new CommentUpdateRequest("수정 내용");
    given(commentService.update(any(), any(), any()))
        .willThrow(CommentNotFoundException.withId(commentId));

    mockMvc.perform(patch("/api/comments/{commentId}", commentId)
            .header("Deokhugam-Request-User-ID", userId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("댓글 수정 실패 - 수정 내용 없음 - 400")
  void updateComment_emptyContent_returns400() throws Exception {
    CommentUpdateRequest request = new CommentUpdateRequest("");

    mockMvc.perform(patch("/api/comments/{commentId}", commentId)
            .header("Deokhugam-Request-User-ID", userId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("댓글 논리 삭제 성공 - 204 반환")
  void softDeleteComment_success() throws Exception {
    mockMvc.perform(delete("/api/comments/{commentId}", commentId)
            .header("Deokhugam-Request-User-ID", userId.toString()))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("댓글 논리삭제 실패 - 헤더 누락 - 400")
  void softDeleteComment_missingHeader_returns400() throws Exception {
    mockMvc.perform(delete("/api/comments/{commentId}", commentId))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("댓글 논리삭제 실패 - 타인 댓글 - 403")
  void softDeleteComment_forbidden_returns403() throws Exception {
    willThrow(new ForbiddenAuthorityException(ErrorCode.COMMENT_DELETE_FORBIDDEN))
        .given(commentService).softDelete(any(), any());

    mockMvc.perform(delete("/api/comments/{commentId}", commentId)
            .header("Deokhugam-Request-User-ID", UUID.randomUUID().toString()))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("댓글 논리삭제 실패 - 존재하지 않는 댓글 - 404")
  void softDeleteComment_notFound_returns404() throws Exception {
    willThrow(CommentNotFoundException.withId(commentId))
        .given(commentService).softDelete(any(), any());

    mockMvc.perform(delete("/api/comments/{commentId}", commentId)
            .header("Deokhugam-Request-User-ID", userId.toString()))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("댓글 논리 삭제 실패 - 이미 삭제된 댓글이면 410 반환")
  void softDeleteComment_alreadyDeleted_returns410() throws Exception {
    willThrow(new CustomException(ErrorCode.DELETED_COMMENT))
        .given(commentService).softDelete(any(), any());

    mockMvc.perform(delete("/api/comments/{commentId}", commentId)
            .header("Deokhugam-Request-User-ID", userId.toString()))
        .andExpect(status().isGone());
  }

  @Test
  @DisplayName("댓글 물리 삭제 성공 - 204 반환")
  void hardDeleteComment_success() throws Exception {
    mockMvc.perform(delete("/api/comments/{commentId}/hard", commentId)
            .header("Deokhugam-Request-User-ID", userId.toString()))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("댓글 물리삭제 실패 - 헤더 누락 - 400")
  void hardDeleteComment_missingHeader_returns400() throws Exception {
    mockMvc.perform(delete("/api/comments/{commentId}/hard", commentId))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("댓글 물리삭제 실패 - 타인 댓글 - 403")
  void hardDeleteComment_forbidden_returns403() throws Exception {
    willThrow(new ForbiddenAuthorityException(ErrorCode.COMMENT_DELETE_FORBIDDEN))
        .given(commentService).hardDelete(any(), any());

    mockMvc.perform(delete("/api/comments/{commentId}/hard", commentId)
            .header("Deokhugam-Request-User-ID", UUID.randomUUID().toString()))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("댓글 물리삭제 실패 - 존재하지 않는 댓글 - 404")
  void hardDeleteComment_notFound_returns404() throws Exception {
    willThrow(CommentNotFoundException.withId(commentId))
        .given(commentService).hardDelete(any(), any());

    mockMvc.perform(delete("/api/comments/{commentId}/hard", commentId)
            .header("Deokhugam-Request-User-ID", userId.toString()))
        .andExpect(status().isNotFound());

  }

}