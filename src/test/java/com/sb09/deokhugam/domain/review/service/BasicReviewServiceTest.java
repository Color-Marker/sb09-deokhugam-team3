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
import com.sb09.deokhugam.domain.review.dto.request.ReviewListRequest;
import com.sb09.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.sb09.deokhugam.domain.review.dto.response.ReviewDto;
import com.sb09.deokhugam.domain.review.dto.response.ReviewLikeDto;
import com.sb09.deokhugam.domain.review.entity.Review;
import com.sb09.deokhugam.domain.review.entity.ReviewLike;
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
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import com.sb09.deokhugam.global.common.mapper.CursorPageResponseMapper;

import static org.mockito.Mockito.doReturn;

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
import org.springframework.data.domain.Slice;

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
    given(book.getReviewCount()).willReturn(0);
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
    // given
    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
    given(userRepository.findById(userId)).willReturn(Optional.of(users));
    given(reviewRepository.existsByBookIdAndUserId(bookId, userId)).willReturn(false);
    given(reviewRepository.save(any(Review.class))).willReturn(review);

    ReviewCreateRequest request = new ReviewCreateRequest(bookId, "너무 재밌어요!", 5);

    // when
    reviewService.createReview(request, userId);

    // then
    verify(reviewRepository, times(1)).save(any(Review.class));
  }

  @Test
  @DisplayName("예외 검증 - 이미 작성한 리뷰가 있으면 예외 발생")
  void createReview_duplicate() {
    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
    given(userRepository.findById(userId)).willReturn(Optional.of(users));
    given(reviewRepository.existsByBookIdAndUserId(bookId, userId)).willReturn(true);

    ReviewCreateRequest request = new ReviewCreateRequest(bookId, "내용", 5);

    assertThatThrownBy(() -> reviewService.createReview(request, userId))
        .isInstanceOf(DuplicateReviewException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.DUPLICATE_REVIEW));
  }

  @Test
  @DisplayName("예외 검증 - 타인의 리뷰 수정 시도 시 예외 발생")
  void updateReview_notOwner() {
    UUID otherUserId = UUID.randomUUID();
    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

    ReviewUpdateRequest request = new ReviewUpdateRequest("수정내용", 4);

    assertThatThrownBy(() -> reviewService.updateReview(reviewId, request, otherUserId))
        .isInstanceOf(ReviewForbiddenException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.UNAUTHORIZED_ACCESS));
  }

  @Test
  @DisplayName("예외 검증 - 존재하지 않는 리뷰 삭제 시도 시 예외 발생")
  void deleteReview_notFound() {
    given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> reviewService.deleteReview(reviewId, userId))
        .isInstanceOf(ReviewNotFoundException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.REVIEW_NOT_FOUND));
  }

  @Test
  @DisplayName("예외 검증 - 이미 삭제된 리뷰 재삭제 시도 시 예외 발생")
  void deleteReview_alreadyDeleted() {
    given(review.getDeletedAt()).willReturn(LocalDateTime.now());
    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

    assertThatThrownBy(() -> reviewService.deleteReview(reviewId, userId))
        .isInstanceOf(ReviewAlreadyDeletedException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.DELETED_REVIEW));
  }

  @Test
  @DisplayName("리뷰 목록 조회 성공 테스트 (DTO 직접 조회 검증)")
  @SuppressWarnings("unchecked")
  void getReviews_success() {
    // given
    UUID currentUserId = UUID.randomUUID();
    ReviewListRequest request = new ReviewListRequest(null, null, null, 10, null, null, null, null);

    Slice<ReviewDto> mockSlice = mock(Slice.class);
    CursorPageResponseDto<ReviewDto> mockResponse = mock(CursorPageResponseDto.class);

    //  레포지토리가 searchReviews 호출 시 mockSlice를 반환하도록 설정
    given(reviewRepository.searchReviews(request, currentUserId)).willReturn(mockSlice);
    //  매퍼가 fromSlice 호출 시 mockResponse를 반환하도록 설정
    doReturn(mockResponse)
        .when(cursorPageResponseMapper)
        .fromSlice(any(), any(), any(), any(), any());

    // when
    CursorPageResponseDto<ReviewDto> result = reviewService.getReviews(request, currentUserId);

    // then
    // 결과가 널이 아닌지 확인하고, 레포지토리의 searchReviews가 정확히 1번 호출되었는지 검증합니다.
    assertThat(result).isNotNull();
    verify(reviewRepository, times(1)).searchReviews(request, currentUserId);

    // N+1 문제 해결 검증 확인
    verify(bookRepository, times(0)).findById(any());
    verify(userRepository, times(0)).findById(any());
  }

  @Test
  @DisplayName("리뷰 좋아요 성공 테스트 - 기존에 누른 적이 없으면 [추가]된다")
  void toggleLike_addLike() {
    // given
    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
    given(userRepository.findById(userId)).willReturn(Optional.of(users));

    // 이전에 좋아요를 누른 기록이 '없음(empty)'을 모방
    given(reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId))
        .willReturn(Optional.empty());

    // 좋아요 추가 후 카운트가 1이 되었다고 가정
    given(review.getLikeCount()).willReturn(1);

    // when
    ReviewLikeDto result = reviewService.toggleLike(reviewId, userId);

    // then
    // 1. save가 1번 호출되었는지 검증 (새로 저장됨)
    verify(reviewLikeRepository, times(1)).save(any(ReviewLike.class));
    // 2. 리뷰 엔티티의 addLikeCount가 호출되었는지 검증
    verify(review, times(1)).addLikeCount();
    // 3. 결과 DTO가 liked=true 인지 검증
    assertThat(result.liked()).isTrue();
    assertThat(result.likeCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("리뷰 좋아요 성공 테스트 - 기존에 누른 적이 있으면 [취소]된다")
  void toggleLike_removeLike() {
    // given
    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
    given(userRepository.findById(userId)).willReturn(Optional.of(users));

    // 가짜 좋아요 객체 생성
    ReviewLike mockExistingLike = mock(ReviewLike.class);
    // 이전에 좋아요를 누른 기록이 '있음'을 모방
    given(reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId))
        .willReturn(Optional.of(mockExistingLike));

    // 좋아요 취소 후 카운트가 0이 되었다고 가정
    given(review.getLikeCount()).willReturn(0);

    // when
    ReviewLikeDto result = reviewService.toggleLike(reviewId, userId);

    // then
    // 1. delete가 1번 호출되었는지 검증 (기존 기록 삭제됨)
    verify(reviewLikeRepository, times(1)).delete(mockExistingLike);
    // 2. 리뷰 엔티티의 removeLikeCount가 호출되었는지 검증
    verify(review, times(1)).removeLikeCount();
    // 3. 결과 DTO가 liked=false 인지 검증
    assertThat(result.liked()).isFalse();
    assertThat(result.likeCount()).isEqualTo(0);
  }
}