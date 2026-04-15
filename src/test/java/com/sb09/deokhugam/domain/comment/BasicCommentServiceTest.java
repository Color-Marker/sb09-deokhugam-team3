package com.sb09.deokhugam.domain.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.sb09.deokhugam.domain.comment.entity.Comment;
import com.sb09.deokhugam.domain.comment.mapper.CommentMapper;
import com.sb09.deokhugam.domain.comment.repository.CommentRepository;
import com.sb09.deokhugam.domain.comment.service.CommentService;
import com.sb09.deokhugam.domain.comment.service.basic.BasicCommentService;
import com.sb09.deokhugam.domain.review.entity.Review;
import com.sb09.deokhugam.domain.review.repository.ReviewRepository;
import com.sb09.deokhugam.domain.user.entity.Users;
import com.sb09.deokhugam.domain.user.repository.UserRepository;
import com.sb09.deokhugam.global.Exception.CustomException;
import com.sb09.deokhugam.global.Exception.ErrorCode;
import com.sb09.deokhugam.global.Exception.comment.CommentNotFoundException;
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

@ExtendWith(MockitoExtension.class)
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
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;

  @BeforeEach
  void setUp() {
    reviewId = UUID.randomUUID();
    userId = UUID.randomUUID();
    commentId = UUID.randomUUID();
    content = "test content";
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    deletedAt = null;

    users = mock(Users.class);
    given(users.getId()).willReturn(reviewId);
    given(users.getDeletedAt()).willReturn(null);

    review = mock(Review.class);
    given(review.getId()).willReturn(userId);
    given(review.getDeletedAt()).willReturn(null);

    comment = mock(Comment.class);
    given(comment.getId()).willReturn(commentId);
    given(comment.getUserId()).willReturn(users);
    given(comment.getReviewId()).willReturn(review);
    given(comment.getContent()).willReturn(content);
    given(comment.getDeletedAt()).willReturn(null);


  }


  @Test
  @DisplayName("논리 삭제 검증 - 댓글 단건 조회 - 예외 발생")
  void deletedComment_throwException() {
    given(comment.getDeletedAt()).willReturn(LocalDateTime.now());
    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

    assertThatThrownBy(() -> commentService.findById(commentId))
        .isInstanceOf(CommentNotFoundException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.DELETED_COMMENT));
  }

}
