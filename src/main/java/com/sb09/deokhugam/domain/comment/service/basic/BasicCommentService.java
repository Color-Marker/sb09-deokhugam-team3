package com.sb09.deokhugam.domain.comment.service.basic;

import com.sb09.deokhugam.domain.comment.dto.CommentDto;
import com.sb09.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.sb09.deokhugam.domain.comment.dto.request.CommentListRequest;
import com.sb09.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.sb09.deokhugam.domain.comment.entity.Comment;
import com.sb09.deokhugam.domain.comment.mapper.CommentMapper;
import com.sb09.deokhugam.domain.comment.repository.CommentRepository;
import com.sb09.deokhugam.domain.comment.service.CommentService;
import com.sb09.deokhugam.global.Exception.review.ReviewAlreadyDeletedException;
import com.sb09.deokhugam.global.Exception.review.ReviewNotFoundException;
import com.sb09.deokhugam.global.Exception.user.UserAlreadyDeletedException;
import com.sb09.deokhugam.global.Exception.user.UserNotFoundException;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import com.sb09.deokhugam.global.common.mapper.CursorPageResponseMapper;
import com.sb09.deokhugam.domain.notification.entity.NotificationType;
import com.sb09.deokhugam.domain.notification.service.NotificationService;
import com.sb09.deokhugam.domain.review.entity.Review;
import com.sb09.deokhugam.domain.review.repository.ReviewRepository;
import com.sb09.deokhugam.domain.user.entity.Users;
import com.sb09.deokhugam.domain.user.repository.UserRepository;
import com.sb09.deokhugam.global.Exception.ErrorCode;
import com.sb09.deokhugam.global.Exception.comment.CommentAlreadyDeletedException;
import com.sb09.deokhugam.global.Exception.comment.CommentNotFoundException;
import com.sb09.deokhugam.global.Exception.comment.ForbiddenAuthorityException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicCommentService implements CommentService {

  private final CommentRepository commentRepository;
  private final UserRepository userRepository;
  private final ReviewRepository reviewRepository;
  private final CommentMapper commentMapper;
  private final NotificationService notificationService;
  private final CursorPageResponseMapper cursorPageResponseMapper;

  @Override
  @Transactional
  public CommentDto create(CommentCreateRequest request) {
    log.debug("댓글 생성 시작: {}", request);
    UUID reviewId = request.reviewId();
    UUID userId = request.userId();

    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> ReviewNotFoundException.withId(reviewId));

    if (review.getDeletedAt() != null) {
      throw ReviewAlreadyDeletedException.withId(reviewId);
    }
    Users user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));

    if (user.getDeletedAt() != null) {
      throw UserAlreadyDeletedException.withId(userId);
    }

    String content = request.content();
    Comment comment = new Comment(review, user, content);

    commentRepository.save(comment);
    review.addCommentCount();

    log.info("리뷰ID: {} 에 대한 유저ID: {} 의 댓글 작성 알람을 생성합니다.", reviewId, userId);
    notificationService.create(NotificationType.COMMENT, review, user);

    log.info("댓글 생성 완료: 리뷰 id={}, 유저 id={}", reviewId, userId);
    return commentMapper.toDto(comment);
  }

  @Override
  @Transactional(readOnly = true)
  public CommentDto findById(UUID commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> CommentNotFoundException.withId(commentId));

    if (comment.getDeletedAt() != null) {
      throw CommentAlreadyDeletedException.withId(commentId);
    }
    return commentMapper.toDto(comment);
  }

  @Override
  @Transactional(readOnly = true)
  public CursorPageResponseDto<CommentDto> findAllByReviewId(CommentListRequest request) {
    PageRequest pageable = PageRequest.of(0, request.getLimit());
    Slice<Comment> slice;
    if (request.getDirection() == Sort.Direction.ASC) {
      slice = commentRepository.findCommentsAsc(
          request.getReviewId(),
          request.getAfter(),
          request.getCursor(),
          pageable);
    } else {
      slice = commentRepository.findCommentsDesc(
          request.getReviewId(),
          request.getAfter(),
          request.getCursor(),
          pageable);
    }
    Long totalElements = commentRepository.countByReviewIdAndDeletedAtIsNull(request.getReviewId());
    return cursorPageResponseMapper.fromSlice(
        slice,
        commentMapper::toDto,
        Comment::getId,
        Comment::getCreatedAt,
        totalElements
    );
  }

  @Override
  @Transactional
  public CommentDto update(UUID commentId, UUID requestUserId, CommentUpdateRequest request) {
    log.debug("댓글 수정 시작: id={}, request={}", commentId, request);
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> CommentNotFoundException.withId(commentId));
    if (comment.getDeletedAt() != null) {
      throw CommentAlreadyDeletedException.withId(commentId);
    }
    if (!comment.getUser().getId().equals(requestUserId)) {
      throw new ForbiddenAuthorityException(ErrorCode.COMMENT_UPDATE_FORBIDDEN);
    }
    comment.updateContent(request.content());
    log.info("댓글 수정 완료: id={}, reviewID={}", commentId, comment.getReview().getId());
    return commentMapper.toDto(comment);
  }

  @Override
  @Transactional
  public void softDelete(UUID commentId, UUID requestUserId) {
    log.debug("댓글 논리삭제 시작: id={}, requestUserId={}", commentId, requestUserId);
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> CommentNotFoundException.withId(commentId));

    if (comment.getDeletedAt() != null) {
      throw new CommentAlreadyDeletedException();
    }
    if (!comment.getUser().getId().equals(requestUserId)) {
      throw new ForbiddenAuthorityException(ErrorCode.COMMENT_DELETE_FORBIDDEN);
    }
    comment.markAsDeleted();
    comment.getReview().removeCommentCount();
    log.info("댓글 논리삭제 완료: id={}", commentId);
  }

  @Override
  @Transactional
  public void hardDelete(UUID commentId, UUID requestUserId) {
    log.debug("댓글 물리삭제 시작: id={}, requestUserId={}", commentId, requestUserId);
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> CommentNotFoundException.withId(commentId));

    if (!comment.getUser().getId().equals(requestUserId)) {
      throw new ForbiddenAuthorityException(ErrorCode.COMMENT_DELETE_FORBIDDEN);
    }
    if (comment.getDeletedAt() == null) {
      comment.getReview().removeCommentCount();
    }
    commentRepository.delete(comment);
    log.info("댓글 물리삭제 완료: id={}", commentId);
  }
}
