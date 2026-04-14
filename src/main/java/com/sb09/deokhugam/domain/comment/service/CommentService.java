package com.sb09.deokhugam.domain.comment.service;

import com.sb09.deokhugam.domain.comment.dto.CommentDto;
import com.sb09.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.sb09.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import java.util.List;
import java.util.UUID;

public interface CommentService {

  CommentDto create(CommentCreateRequest request);

  CommentDto findById(UUID commentId);

  List<CommentDto> findAllByReviewId(UUID reviewId);

  CommentDto update(UUID commentId, CommentUpdateRequest request);

  void softDelete(UUID commentId);

  void hardDelete(UUID commentId);
}
