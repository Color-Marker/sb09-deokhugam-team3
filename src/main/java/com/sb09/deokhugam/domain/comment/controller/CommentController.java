package com.sb09.deokhugam.domain.comment.controller;

import com.sb09.deokhugam.domain.comment.controller.api.CommentApi;
import com.sb09.deokhugam.domain.comment.dto.CommentDto;
import com.sb09.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.sb09.deokhugam.domain.comment.dto.request.CommentListRequest;
import com.sb09.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.sb09.deokhugam.domain.comment.service.CommentService;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController implements CommentApi {

  private final CommentService commentService;

  @PostMapping
  public ResponseEntity<CommentDto> createComment(
      @Valid @RequestBody CommentCreateRequest request
  ) {
    log.info("댓글 생성 요청: {}", request);
    CommentDto created = commentService.create(request);
    log.info("생성된 댓글 응답: {}", created);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @GetMapping("/{commentId}")
  public ResponseEntity<CommentDto> getComment(
      @PathVariable UUID commentId
  ) {
    CommentDto comment = commentService.findById(commentId);
    return ResponseEntity.ok(comment);
  }

  @GetMapping
  public ResponseEntity<CursorPageResponseDto<CommentDto>> getComments(
      @Valid CommentListRequest request
  ) {
    CursorPageResponseDto<CommentDto> comments = commentService.findAllByReviewId(request);
    return ResponseEntity.ok(comments);
  }

  @PatchMapping("/{commentId}")
  public ResponseEntity<CommentDto> updateComment(
      @PathVariable UUID commentId,
      @RequestHeader("X-User-Id") UUID requestUserId,
      @Valid @RequestBody CommentUpdateRequest request
  ) {
    log.info("댓글 수정 요청: {}", request);
    CommentDto updated = commentService.update(commentId, requestUserId, request);
    log.info("수정된 댓글 응답: {}", updated);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> softDeleteComment(
      @PathVariable UUID commentId,
      @RequestHeader("X-User-Id") UUID requestUserId
  ) {
    log.info("댓글 논리삭제 요청: 댓글 Id={}, 요청자 Id={}", commentId, requestUserId);
    commentService.softDelete(commentId, requestUserId);
    log.info("댓글 논리삭제 응답: 댓글 Id={}", commentId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{commentId}/hard")
  public ResponseEntity<Void> hardDeleteComment(
      @PathVariable UUID commentId,
      @RequestHeader("X-User-Id") UUID requestUserId
  ) {
    log.info("댓글 물리삭제 요청: 댓글 Id={}, 요청자 Id={}", commentId, requestUserId);
    commentService.hardDelete(commentId, requestUserId);
    log.info("댓글 물리삭제 응답: 댓글 Id={}", commentId);
    return ResponseEntity.noContent().build();
  }
}