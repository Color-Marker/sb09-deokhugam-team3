package com.sb09.deokhugam.domain.comment.controller;

import com.sb09.deokhugam.domain.comment.controller.api.CommentApi;
import com.sb09.deokhugam.domain.comment.dto.CommentDto;
import com.sb09.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.sb09.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.sb09.deokhugam.domain.comment.service.CommentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController implements CommentApi {

  private final CommentService commentService;

  @GetMapping
  public ResponseEntity<List<CommentDto>> getComments(
      @RequestParam UUID reviewId
  ) {
    List<CommentDto> comments = commentService.findAllByReviewId(reviewId);
    return ResponseEntity.ok(comments);
  }

  @PostMapping
  public ResponseEntity<CommentDto> createComment(
      @Valid @RequestBody CommentCreateRequest request
  ) {
    CommentDto created = commentService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created); // 생성 성공은 201 반환
  }

  @GetMapping("/{commentId}")
  public ResponseEntity<CommentDto> getComment(
      @PathVariable UUID commentId
  ) {
    CommentDto comment = commentService.findById(commentId);
    return ResponseEntity.ok(comment);
  }

  @PatchMapping("/{commentId}")
  public ResponseEntity<CommentDto> updateComment(
      @PathVariable UUID commentId,
      @Valid @RequestBody CommentUpdateRequest request
  ) {
    CommentDto updated = commentService.update(commentId, request);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> softDeleteComment(
      @PathVariable UUID commentId
  ) {
    commentService.softDelete(commentId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{commentId}/hard")
  public ResponseEntity<Void> hardDeleteComment(
      @PathVariable UUID commentId
  ) {
    commentService.hardDelete(commentId);
    return ResponseEntity.noContent().build();
  }
}