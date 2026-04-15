package com.sb09.deokhugam.domain.comment.controller.api;

import com.sb09.deokhugam.domain.comment.dto.CommentDto;
import com.sb09.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.sb09.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Comment", description = "댓글 관련 API")
public interface CommentApi {

  @Operation(summary = "댓글 목록 조회", description = "특정 리뷰의 댓글 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
  })
  ResponseEntity<List<CommentDto>> getComments(
      @Parameter(description = "리뷰 ID") UUID reviewId
  );

  @Operation(summary = "댓글 등록", description = "리뷰에 댓글을 등록합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "등록 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
  })
  ResponseEntity<CommentDto> createComment(
      @RequestBody CommentCreateRequest request
  );

  @Operation(summary = "댓글 단건 조회", description = "댓글 ID로 특정 댓글을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
  })
  ResponseEntity<CommentDto> getComment(
      @Parameter(description = "댓글 ID") @PathVariable UUID commentId
  );

  @Operation(summary = "댓글 수정", description = "댓글 내용을 수정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "수정 성공"),
      @ApiResponse(responseCode = "403", description = "수정 권한 없음"),
      @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
  })
  ResponseEntity<CommentDto> updateComment(
      @Parameter(description = "댓글 ID") @PathVariable UUID commentId,
      @RequestBody CommentUpdateRequest request
  );

  @Operation(summary = "댓글 논리 삭제", description = "댓글을 논리 삭제합니다. (deleted_at 기록)")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "삭제 성공"),
      @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
      @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
  })
  ResponseEntity<Void> softDeleteComment(
      @Parameter(description = "댓글 ID") @PathVariable UUID commentId
  );

  @Operation(summary = "댓글 물리 삭제", description = "댓글을 DB에서 완전히 삭제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "삭제 성공"),
      @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
      @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
  })
  ResponseEntity<Void> hardDeleteComment(
      @Parameter(description = "댓글 ID") @PathVariable UUID commentId
  );
}
