package com.sb09.deokhugam.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import com.sb09.deokhugam.domain.book.entity.Book;
import com.sb09.deokhugam.domain.book.repository.BookRepository;
import com.sb09.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sb09.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.sb09.deokhugam.domain.review.entity.Review;
import com.sb09.deokhugam.domain.review.mapper.ReviewMapper;
import com.sb09.deokhugam.domain.review.repository.ReviewLikeRepository;
import com.sb09.deokhugam.domain.review.repository.ReviewRepository;
import com.sb09.deokhugam.domain.review.service.basic.BasicReviewService;
import com.sb09.deokhugam.domain.user.entity.Users;
import com.sb09.deokhugam.domain.user.repository.UserRepository;

import com.sb09.deokhugam.global.Exception.CustomException;
import com.sb09.deokhugam.global.Exception.ErrorCode;
import com.sb09.deokhugam.global.Exception.review.DuplicateReviewException;
import com.sb09.deokhugam.global.Exception.review.ReviewAlreadyDeletedException;
import com.sb09.deokhugam.global.Exception.review.ReviewForbiddenException;
import com.sb09.deokhugam.global.Exception.review.ReviewNotFoundException;
import com.sb09.deokhugam.global.common.mapper.CursorPageResponseMapper;

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
public class BasicReviewServiceTest {

  @Mock
  private ReviewRepository reviewRepository;
  @Mock
  private BookRepository bookRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private ReviewLikeRepository reviewLikeRepository;
  @Mock
  private ReviewMapper reviewMapper;
  @Mock
  private CursorPageResponseMapper cursorPageResponseMapper;

  @InjectMocks
  private BasicReviewService reviewService;

  private UUID bookId;
  private UUID userId;
  private UUID reviewId;
  private Book book;
  private Users users;
  private Review review;

  @BeforeEach
  void setUp() {
    bookId = UUID.randomUUID();
    userId = UUID.randomUUID();
    reviewId = UUID.randomUUID();

    // Mock(가짜) 객체 생성
    book = mock(Book.class);
    users = mock(Users.class);
    review = mock(Review.class);

    // Mock 객체들의 기본 행동 설정
    given(book.getId()).willReturn(bookId);
    given(book.getReviewCount()).willReturn(0); // 통계 업데이트를 위한 기본값
    given(book.getRating()).willReturn(java.math.BigDecimal.ZERO);

    given(users.getId()).willReturn(userId);

    given(review.getId()).willReturn(reviewId);
    given(review.getUserId()).willReturn(userId);
    given(review.getBookId()).willReturn(bookId);
    given(review.getDeletedAt()).willReturn(null);
  }

  @Test
  @DisplayName("리뷰 등록 성공 테스트")
  void createReview_success() {
    // given: DB에 책과 유저가 정상적으로 존재하고, 리뷰는 아직 안 쓴 상태
    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
    given(userRepository.findById(userId)).willReturn(Optional.of(users));
    given(reviewRepository.existsByBookIdAndUserId(bookId, userId)).willReturn(false);

    ReviewCreateRequest request = new ReviewCreateRequest(bookId, "너무 재밌어요!", 5);

    // when: 리뷰 생성 로직 실행
    reviewService.createReview(request, userId);

    // then: reviewRepository.save()가 딱 1번 호출되었는지 검증 (진짜로 저장 요청을 보냈는지 확인)
    verify(reviewRepository, times(1)).save(any(Review.class));
  }

  @Test
  @DisplayName("예외 검증 - 이미 작성한 리뷰가 있으면 예외 발생 (DuplicateReviewException)")
  void createReview_duplicate() {
    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
    given(userRepository.findById(userId)).willReturn(Optional.of(users));

    // DB에 이미 리뷰가 있다고 세팅
    given(reviewRepository.existsByBookIdAndUserId(bookId, userId)).willReturn(true);

    ReviewCreateRequest request = new ReviewCreateRequest(bookId, "내용", 5);

    assertThatThrownBy(() -> reviewService.createReview(request, userId))
        .isInstanceOf(DuplicateReviewException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.DUPLICATE_REVIEW));
  }

  @Test
  @DisplayName("예외 검증 - 타인의 리뷰 수정 시도 시 예외 발생 (ReviewForbiddenException)")
  void updateReview_notOwner() {
    UUID otherUserId = UUID.randomUUID();
    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
    // 현재 리뷰의 작성자는 userId로 세팅되어 있음

    ReviewUpdateRequest request = new ReviewUpdateRequest("수정내용", 4);

    assertThatThrownBy(() -> reviewService.updateReview(reviewId, request, otherUserId))
        .isInstanceOf(ReviewForbiddenException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.UNAUTHORIZED_ACCESS));
  }

  @Test
  @DisplayName("예외 검증 - 존재하지 않는 리뷰 삭제 시도 시 예외 발생 (ReviewNotFoundException)")
  void deleteReview_notFound() {
    // DB에서 리뷰를 찾지 못함 (Optional.empty())
    given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> reviewService.deleteReview(reviewId, userId))
        .isInstanceOf(ReviewNotFoundException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.REVIEW_NOT_FOUND));
  }

  @Test
  @DisplayName("예외 검증 - 이미 삭제된 리뷰 재삭제 시도 시 예외 발생 (ReviewAlreadyDeletedException)")
  void deleteReview_alreadyDeleted() {
    // 이미 논리 삭제(deletedAt) 시간이 찍혀있다고 가정
    given(review.getDeletedAt()).willReturn(LocalDateTime.now());
    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

    assertThatThrownBy(() -> reviewService.deleteReview(reviewId, userId))
        .isInstanceOf(ReviewAlreadyDeletedException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.DELETED_REVIEW));
  }
}