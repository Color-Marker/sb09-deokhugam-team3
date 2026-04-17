package com.sb09.deokhugam.domain.comment.service;

import com.sb09.deokhugam.domain.comment.dto.CommentDto;
import com.sb09.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.sb09.deokhugam.domain.comment.dto.request.CommentListRequest;
import com.sb09.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import java.util.UUID;

public interface CommentService {

  CommentDto create(CommentCreateRequest request);

  CommentDto findById(UUID commentId);

  CursorPageResponseDto<CommentDto> findAllByReviewId(CommentListRequest request);

  CommentDto update(UUID commentId, UUID requestUserId, CommentUpdateRequest request);

  void softDelete(UUID commentId, UUID requestUserId);

  void hardDelete(UUID commentId, UUID requestUserId);
}
