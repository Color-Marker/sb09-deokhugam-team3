package com.sb09.deokhugam.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.sb09.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.sb09.deokhugam.domain.comment.entity.Comment;
import com.sb09.deokhugam.domain.comment.mapper.CommentMapper;
import com.sb09.deokhugam.domain.comment.repository.CommentRepository;
import com.sb09.deokhugam.domain.comment.service.basic.BasicCommentService;
import com.sb09.deokhugam.domain.review.entity.Review;
import com.sb09.deokhugam.domain.review.repository.ReviewRepository;
import com.sb09.deokhugam.domain.user.entity.Users;
import com.sb09.deokhugam.domain.user.repository.UserRepository;
import com.sb09.deokhugam.global.Exception.CustomException;
import com.sb09.deokhugam.global.Exception.ErrorCode;
import com.sb09.deokhugam.global.Exception.comment.CommentAlreadyDeletedException;
import com.sb09.deokhugam.global.Exception.comment.CommentNotFoundException;
import com.sb09.deokhugam.global.Exception.comment.ForbiddenAuthorityException;
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

  @InjectMocks
  private BasicCommentService commentService;

  private UUID reviewId;
  private UUID userId;
  private UUID commentId;
  private String content;
  private Users users;
  private Review review;
  private Comment comment;

  @BeforeEach
  void setUp() {
    reviewId = UUID.randomUUID();
    userId = UUID.randomUUID();
    commentId = UUID.randomUUID();
    content = "test content";

    users = mock(Users.class);
    review = mock(Review.class);
    comment = mock(Comment.class);

    given(users.getId()).willReturn(userId);
    given(users.getDeletedAt()).willReturn(null);

    given(review.getId()).willReturn(reviewId);
    given(review.getDeletedAt()).willReturn(null);

    given(comment.getId()).willReturn(commentId);
    given(comment.getUser()).willReturn(users);   // ← 먼저 users 객체를 반환하게
    given(comment.getReview()).willReturn(review);
    given(comment.getDeletedAt()).willReturn(null);
    given(comment.getContent()).willReturn(content);
  }

  @Test
  @DisplayName("논리 삭제 검증 - 삭제 댓글 단건 조회 - 예외 발생")
  void softDelete_findById() {
    given(comment.getDeletedAt()).willReturn(LocalDateTime.now());
    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

    assertThatThrownBy(() -> commentService.findById(commentId))
        .isInstanceOf(CustomException.class)
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
        .isInstanceOf(CustomException.class)
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
    // given - DB에 없는 commentId 상황
    UUID notExistId = UUID.randomUUID();
    given(commentRepository.findById(notExistId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> commentService.findById(notExistId))
        .isInstanceOf(CustomException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.COMMENT_NOT_FOUND));
  }

  @Test
  @DisplayName("존재하지 않는 댓글 수정 - 예외 발생")
  void update_notExist() {
    // given
    UUID notExistId = UUID.randomUUID();
    given(commentRepository.findById(notExistId)).willReturn(Optional.empty());

    CommentUpdateRequest request = new CommentUpdateRequest("수정 내용");

    // when & then
    assertThatThrownBy(() -> commentService.update(notExistId, userId, request))
        .isInstanceOf(CustomException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.COMMENT_NOT_FOUND));
  }

  @Test
  @DisplayName("존재하지 않는 댓글 논리 삭제 - 예외 발생")
  void softDelete_notExist() {
    // given
    UUID notExistId = UUID.randomUUID();
    given(commentRepository.findById(notExistId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> commentService.softDelete(notExistId, userId))
        .isInstanceOf(CustomException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.COMMENT_NOT_FOUND));
  }

  @Test
  @DisplayName("존재하지 않는 댓글 물리 삭제 - 예외 발생")
  void hardDelete_notExist() {
    // given
    UUID notExistId = UUID.randomUUID();
    given(commentRepository.findById(notExistId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> commentService.hardDelete(notExistId, userId))
        .isInstanceOf(CustomException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.COMMENT_NOT_FOUND));
  }


}
