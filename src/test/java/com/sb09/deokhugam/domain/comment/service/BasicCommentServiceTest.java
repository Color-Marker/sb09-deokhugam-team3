package com.sb09.deokhugam.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sb09.deokhugam.domain.comment.dto.CommentDto;
import com.sb09.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.sb09.deokhugam.domain.comment.dto.request.CommentListRequest;
import com.sb09.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.sb09.deokhugam.domain.comment.entity.Comment;
import com.sb09.deokhugam.domain.comment.mapper.CommentMapper;
import com.sb09.deokhugam.domain.comment.repository.CommentRepository;
import com.sb09.deokhugam.domain.comment.service.basic.BasicCommentService;
import com.sb09.deokhugam.domain.notification.service.NotificationService;
import com.sb09.deokhugam.domain.review.entity.Review;
import com.sb09.deokhugam.domain.review.repository.ReviewRepository;
import com.sb09.deokhugam.domain.user.entity.Users;
import com.sb09.deokhugam.domain.user.repository.UserRepository;
import com.sb09.deokhugam.global.exception.user.UserAlreadyDeletedException;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import com.sb09.deokhugam.global.common.mapper.CursorPageResponseMapper;
import com.sb09.deokhugam.global.exception.CustomException;
import com.sb09.deokhugam.global.exception.ErrorCode;
import com.sb09.deokhugam.global.exception.comment.CommentAlreadyDeletedException;
import com.sb09.deokhugam.global.exception.comment.CommentNotFoundException;
import com.sb09.deokhugam.global.exception.comment.ForbiddenAuthorityException;
import com.sb09.deokhugam.global.exception.review.ReviewAlreadyDeletedException;
import com.sb09.deokhugam.global.exception.review.ReviewNotFoundException;
import com.sb09.deokhugam.global.exception.user.UserNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BasicCommentServiceTest {

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private CommentMapper commentMapper;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private NotificationService notificationService;

  @Mock
  private CursorPageResponseMapper cursorPageResponseMapper;

  @InjectMocks
  private BasicCommentService commentService;

  private UUID reviewId;
  private UUID userId;
  private UUID commentId;
  private String content;
  private Users users;
  private Review review;
  private Comment comment;
  private CommentDto commentDto;
  private CommentListRequest request;
  private CursorPageResponseDto<CommentDto> cursorPageResponseDto;

  @BeforeEach
  void setUp() {
    reviewId = UUID.randomUUID();
    userId = UUID.randomUUID();
    commentId = UUID.randomUUID();
    content = "test content";

    users = mock(Users.class);
    review = mock(Review.class);
    comment = mock(Comment.class);

    request = mock(CommentListRequest.class);
    cursorPageResponseDto = mock(CursorPageResponseDto.class);

    given(users.getId()).willReturn(userId);
    given(users.getDeletedAt()).willReturn(null);

    given(review.getId()).willReturn(reviewId);
    given(review.getDeletedAt()).willReturn(null);

    given(comment.getId()).willReturn(commentId);
    given(comment.getUser()).willReturn(users);   // ← 먼저 users 객체를 반환하게
    given(comment.getReview()).willReturn(review);
    given(comment.getDeletedAt()).willReturn(null);
    given(comment.getContent()).willReturn(content);

    given(request.getReviewId()).willReturn(reviewId);
    given(request.getLimit()).willReturn(10);
    given(request.getAfter()).willReturn(null);
    given(request.getCursor()).willReturn(null);

    commentDto = mock(CommentDto.class);
  }

  @Test
  @DisplayName("논리 삭제 검증 - 삭제 댓글 단건 조회 - 예외 발생")
  void softDelete_findById() {
    given(comment.getDeletedAt()).willReturn(LocalDateTime.now());
    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

    assertThatThrownBy(() -> commentService.findById(commentId))
        .isInstanceOf(CommentAlreadyDeletedException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.DELETED_COMMENT));

  }

  @Test
  @DisplayName("논리 삭제 검증 - 삭제 댓글 재삭제 - 예외 발생")
  void softDelete_alreadyDeleted() {
    given(comment.getDeletedAt()).willReturn(LocalDateTime.now());
    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

    assertThatThrownBy(() -> commentService.softDelete(commentId, userId))
        .isInstanceOf(CommentAlreadyDeletedException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.DELETED_COMMENT));
  }

  @Test
  @DisplayName("논리 삭제 검증 - 존재하지 않는 댓글 삭제 - 예외 발생")
  void softDelete_notExistComment() {
    UUID notExistId = UUID.randomUUID();
    given(commentRepository.findById(commentId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> commentService.softDelete(commentId, notExistId))
        .isInstanceOf(CommentNotFoundException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.COMMENT_NOT_FOUND));
  }

  @Test
  @DisplayName("권한 검증 - 타인 댓글 수정 - 예외 발생")
  void update_notOwner() {
    UUID otherUserId = UUID.randomUUID();
    given(comment.getUser().getId()).willReturn(userId);
    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

    CommentUpdateRequest request = new CommentUpdateRequest("test content");

    assertThatThrownBy(() -> commentService.update(commentId, otherUserId, request))
        .isInstanceOf(ForbiddenAuthorityException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.COMMENT_UPDATE_FORBIDDEN));

  }

  @Test
  @DisplayName("권한 검증 - 타인 댓글 삭제 - 예외 발생")
  void delete_notOwner() {
    UUID otherUserId = UUID.randomUUID();
    given(comment.getUser().getId()).willReturn(userId);
    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

    assertThatThrownBy(() -> commentService.hardDelete(commentId, otherUserId))
        .isInstanceOf(ForbiddenAuthorityException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.COMMENT_DELETE_FORBIDDEN));
  }

  @Test
  @DisplayName("존재하지 않는 댓글 단건 조회 - 예외 발생")
  void findById_notExist() {
    UUID notExistId = UUID.randomUUID();
    given(commentRepository.findById(notExistId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> commentService.findById(notExistId))
        .isInstanceOf(CommentNotFoundException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.COMMENT_NOT_FOUND));
  }

  @Test
  @DisplayName("존재하지 않는 댓글 수정 - 예외 발생")
  void update_notExist() {
    UUID notExistId = UUID.randomUUID();
    given(commentRepository.findById(notExistId)).willReturn(Optional.empty());

    CommentUpdateRequest request = new CommentUpdateRequest("수정 내용");

    assertThatThrownBy(() -> commentService.update(notExistId, userId, request))
        .isInstanceOf(CommentNotFoundException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.COMMENT_NOT_FOUND));
  }

  @Test
  @DisplayName("존재하지 않는 댓글 논리 삭제 - 예외 발생")
  void softDelete_notExist() {
    UUID notExistId = UUID.randomUUID();
    given(commentRepository.findById(notExistId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> commentService.softDelete(notExistId, userId))
        .isInstanceOf(CommentNotFoundException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.COMMENT_NOT_FOUND));
  }

  @Test
  @DisplayName("존재하지 않는 댓글 물리 삭제 - 예외 발생")
  void hardDelete_notExist() {
    UUID notExistId = UUID.randomUUID();
    given(commentRepository.findById(notExistId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> commentService.hardDelete(notExistId, userId))
        .isInstanceOf(CommentNotFoundException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.COMMENT_NOT_FOUND));
  }

  @Test
  @DisplayName("댓글 생성 - 성공")
  void create_success() {
    CommentCreateRequest request = new CommentCreateRequest(reviewId, userId, content);

    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
    given(userRepository.findById(userId)).willReturn(Optional.of(users));
    given(commentRepository.save(any(Comment.class))).willReturn(comment);
    given(commentMapper.toDto(any(Comment.class))).willReturn(commentDto);

    given(commentRepository.save(any(Comment.class))).willReturn(comment);
    given(commentMapper.toDto(any(Comment.class))).willReturn(commentDto);

    CommentDto result = commentService.create(request);

    assertThat(result).isEqualTo(commentDto);
    verify(reviewRepository).findById(reviewId);
    verify(userRepository).findById(userId);
    verify(commentRepository).save(any(Comment.class));
    verify(commentMapper).toDto(any(Comment.class));
  }

  @Test
  @DisplayName("존재하지 않는 리뷰에 댓글 생성 - 예외 발생")
  void create_reviewNotFound() {
    UUID notExistId = UUID.randomUUID();
    CommentCreateRequest request = new CommentCreateRequest(notExistId, userId, content);
    given(reviewRepository.findById(notExistId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> commentService.create(request))
        .isInstanceOf(ReviewNotFoundException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.REVIEW_NOT_FOUND));
    verify(reviewRepository).findById(notExistId);
    verify((userRepository), never()).findById(userId);
  }

  @Test
  @DisplayName("논리삭제된 리뷰에 댓글 생성 - 예외 발생")
  void create_deletedReview() {
    CommentCreateRequest request = new CommentCreateRequest(reviewId, userId, content);
    given(review.getDeletedAt()).willReturn(LocalDateTime.now());
    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

    assertThatThrownBy(() -> commentService.create(request))
        .isInstanceOf(ReviewAlreadyDeletedException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.DELETED_REVIEW));
    verify(reviewRepository).findById(reviewId);
    verify(userRepository, never()).findById(any());
  }

  @Test
  @DisplayName("존재하지 않는 유저가 댓글 생성 - 예외 발생")
  void create_userNotFound() {
    UUID notExistId = UUID.randomUUID();
    CommentCreateRequest request = new CommentCreateRequest(reviewId, notExistId, content);
    given(userRepository.findById(notExistId)).willReturn(Optional.empty());
    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

    assertThatThrownBy(() -> commentService.create(request))
        .isInstanceOf(UserNotFoundException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.USER_NOT_FOUND));

    verify(reviewRepository).findById(reviewId);
    verify(userRepository).findById(notExistId);
    verify(reviewRepository, never()).save(any());
  }

  @Test
  @DisplayName("논리삭제된 유저가 댓글 생성 - 예외 발생")
  void create_deletedUser() {
    CommentCreateRequest request = new CommentCreateRequest(reviewId, userId, content);
    given(users.getDeletedAt()).willReturn(LocalDateTime.now());
    given(userRepository.findById(userId)).willReturn(Optional.of(users));
    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

    assertThatThrownBy(() -> commentService.create(request))
        .isInstanceOf(UserAlreadyDeletedException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.DELETED_USER));

    verify(reviewRepository).findById(reviewId);
    verify(userRepository).findById(userId);
    verify(commentRepository, never()).save(any());
  }

  @Test
  @DisplayName("댓글 조회 - 성공")
  void findById_success() {
    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
    given(commentMapper.toDto(any(Comment.class))).willReturn(commentDto);

    CommentDto result = commentService.findById(commentId);

    assertThat(result).isEqualTo(commentDto);
    verify(commentRepository).findById(commentId);
    verify(commentMapper).toDto(any(Comment.class));
  }

  @Test
  @DisplayName("댓글 수정 - 성공")
  void update_success() {
    CommentUpdateRequest request = new CommentUpdateRequest(content);
    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
    given(commentMapper.toDto(any(Comment.class))).willReturn(commentDto);

    CommentDto result = commentService.update(commentId, userId, request);

    assertThat(result).isEqualTo(commentDto);
    verify(commentRepository).findById(commentId);
    verify(comment).updateContent(content);
    verify(commentMapper).toDto(any(Comment.class));
  }

  @Test
  @DisplayName("삭제된 댓글 수정 시도 - 예외 발생")
  void update_alreadyDeleted() {
    CommentUpdateRequest request = new CommentUpdateRequest(content);
    given(comment.getDeletedAt()).willReturn(LocalDateTime.now());
    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

    assertThatThrownBy(() -> commentService.update(commentId, userId, request))
        .isInstanceOf(CommentAlreadyDeletedException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.DELETED_COMMENT));

    verify(commentRepository).findById(commentId);
    verify(comment, never()).updateContent(any());
    verify(commentMapper, never()).toDto(any());
  }

  @Test
  @DisplayName("댓글 논리 삭제 - 성공")
  void softDelete_success() {
    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

    commentService.softDelete(commentId, userId);

    verify(commentRepository).findById(commentId);
    verify(comment).markAsDeleted();
    verify(review).removeCommentCount();
    verify(commentMapper, never()).toDto(any());
  }

  @Test
  @DisplayName("댓글 물리 삭제 - 성공")
  void hardDelete_success() {
    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

    commentService.hardDelete(commentId, userId);

    verify(commentRepository).findById(commentId);
    verify(commentRepository).delete(comment);
    verify(review).removeCommentCount();
    verify(commentMapper, never()).toDto(any());
  }

  @Test
  @DisplayName("논리삭제된 댓글 물리삭제 - 성공")
    //  (카운트 이중차감 방지 검증)
  void hardDelete_alreadySoftDeleted() {
    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
    given(comment.getDeletedAt()).willReturn(LocalDateTime.now());

    commentService.hardDelete(commentId, userId);

    verify(commentRepository).findById(commentId);
    verify(commentRepository).delete(comment);
    verify(review, never()).removeCommentCount();
  }

  @Test
  @DisplayName("댓글 목록 조회 - DESC 성공")
  void findAllByReviewId_desc_success() {
    Slice<Comment> slice = mock(Slice.class);
    given(request.getDirection()).willReturn(Sort.Direction.DESC);
    given(commentRepository.findCommentsDesc(
        request.getReviewId(),
        request.getAfter(),
        request.getCursor(),
        PageRequest.of(0, request.getLimit())
    )).willReturn(slice);
    given(commentRepository.countByReviewIdAndDeletedAtIsNull(reviewId)).willReturn(1L);
    doReturn(cursorPageResponseDto).when(cursorPageResponseMapper).fromSlice(
        eq(slice), any(), any(), any(), eq(1L));

    CursorPageResponseDto<CommentDto> result = commentService.findAllByReviewId(request);

    assertThat(result).isEqualTo(cursorPageResponseDto);
    verify(commentRepository).findCommentsDesc(
        request.getReviewId(),
        request.getAfter(),
        request.getCursor(),
        PageRequest.of(0, request.getLimit())
    );
    verify(commentRepository, never()).findCommentsAsc(any(), any(), any(), any());
    verify(commentRepository).countByReviewIdAndDeletedAtIsNull(reviewId);
  }

  @Test
  @DisplayName("댓글 목록 조회 - ASC 성공")
  void findAllByReviewId_asc_success() {
    Slice<Comment> slice = mock(Slice.class);
    given(request.getDirection()).willReturn(Sort.Direction.ASC);
    given(commentRepository.findCommentsAsc(
        request.getReviewId(),
        request.getAfter(),
        request.getCursor(),
        PageRequest.of(0, request.getLimit())
    )).willReturn(slice);
    given(commentRepository.countByReviewIdAndDeletedAtIsNull(reviewId)).willReturn(1L);
    doReturn(cursorPageResponseDto).when(cursorPageResponseMapper).fromSlice(
        eq(slice), any(), any(), any(), eq(1L));

    CursorPageResponseDto<CommentDto> result = commentService.findAllByReviewId(request);

    assertThat(result).isEqualTo(cursorPageResponseDto);
    verify(commentRepository).findCommentsAsc(
        request.getReviewId(),
        request.getAfter(),
        request.getCursor(),
        PageRequest.of(0, request.getLimit())
    );
    verify(commentRepository, never()).findCommentsDesc(any(), any(), any(), any());
    verify(commentRepository).countByReviewIdAndDeletedAtIsNull(reviewId);
  }
}
