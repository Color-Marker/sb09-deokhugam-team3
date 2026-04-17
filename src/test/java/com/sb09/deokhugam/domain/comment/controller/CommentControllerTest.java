package com.sb09.deokhugam.domain.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sb09.deokhugam.domain.comment.dto.CommentDto;
import com.sb09.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.sb09.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.sb09.deokhugam.domain.comment.service.CommentService;
import com.sb09.deokhugam.global.Exception.CustomException;
import com.sb09.deokhugam.global.Exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

@WebMvcTest(CommentController.class)
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
        .willThrow(new CustomException(ErrorCode.COMMENT_NOT_FOUND));

    mockMvc.perform(get("/api/comments/{commentId}", UUID.randomUUID()))
        .andExpect(status().isNotFound()); // 404 확인
  }

  @Test
  @DisplayName("댓글 수정 성공 - 200 반환")
  void updateComment_success() throws Exception {
    CommentUpdateRequest request = new CommentUpdateRequest("수정된 내용");
    given(commentService.update(any(), any(), any())).willReturn(commentDto);

    mockMvc.perform(patch("/api/comments/{commentId}", commentId)
            .header("X-User-Id", userId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("댓글 수정 실패 - 권한 없으면 403 반환")
  void updateComment_forbidden_returns403() throws Exception {
    CommentUpdateRequest request = new CommentUpdateRequest("수정된 내용");
    given(commentService.update(any(), any(), any()))
        .willThrow(new CustomException(ErrorCode.COMMENT_UPDATE_FORBIDDEN));

    mockMvc.perform(patch("/api/comments/{commentId}", commentId)
            .header("X-User-Id", UUID.randomUUID().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("댓글 논리 삭제 성공 - 204 반환")
  void softDeleteComment_success() throws Exception {
    mockMvc.perform(delete("/api/comments/{commentId}", commentId)
            .header("X-User-Id", userId.toString()))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("댓글 논리 삭제 실패 - 이미 삭제된 댓글이면 410 반환")
  void softDeleteComment_alreadyDeleted_returns410() throws Exception {
    willThrow(new CustomException(ErrorCode.DELETED_COMMENT))
        .given(commentService).softDelete(any(), any());

    mockMvc.perform(delete("/api/comments/{commentId}", commentId)
            .header("X-User-Id", userId.toString()))
        .andExpect(status().isGone());
  }

  @Test
  @DisplayName("댓글 물리 삭제 성공 - 204 반환")
  void hardDeleteComment_success() throws Exception {
    // when & then
    mockMvc.perform(delete("/api/comments/{commentId}/hard", commentId)
            .header("X-User-Id", userId.toString()))
        .andExpect(status().isNoContent());
  }
}