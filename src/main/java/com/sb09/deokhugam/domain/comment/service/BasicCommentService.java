package com.sb09.deokhugam.domain.comment.service;

import com.sb09.deokhugam.domain.comment.dto.CommentDto;
import com.sb09.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.sb09.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import java.util.List;
import java.util.UUID;

public class BasicCommentService implements CommentService {

  @Override
  public CommentDto create(CommentCreateRequest request) {
    return null;
  }

  @Override
  public CommentDto findById(UUID commentId) {
    return null;
  }

  @Override
  public List<CommentDto> findAllByReviewId(UUID reviewId) {
    return List.of();
  }

  @Override
  public CommentDto update(UUID commentId, CommentUpdateRequest request) {
    return null;
  }

  @Override
  public void softDelete(UUID commentId) {

  }

  @Override
  public void hardDelete(UUID commentId) {

  }
}
