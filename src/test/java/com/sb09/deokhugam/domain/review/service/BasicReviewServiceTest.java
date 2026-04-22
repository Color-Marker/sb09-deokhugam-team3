package com.sb09.deokhugam.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doReturn;

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
import com.sb09.deokhugam.global.Exception.review.ReviewForbiddenException;
import com.sb09.deokhugam.global.Exception.review.ReviewNotFoundException;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import com.sb09.deokhugam.global.common.mapper.CursorPageResponseMapper;

import java.util.List;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

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

    book = mock(Book.class);
    users = mock(Users.class);
    review = mock(Review.class);

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
    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
    given(userRepository.findById(userId)).willReturn(Optional.of(users));
    given(reviewRepository.existsByBookIdAndUserId(bookId, userId)).willReturn(false);
    given(reviewRepository.save(any(Review.class))).willReturn(review);

    ReviewCreateRequest request = new ReviewCreateRequest(userId, bookId, "너무 재밌어요!", 5);

    reviewService.createReview(request);

    verify(reviewRepository, times(1)).save(any(Review.class));
  }

  @Test
  @DisplayName("예외 검증 - 이미 작성한 리뷰가 있으면 예외 발생")
  void createReview_duplicate() {
    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
    given(userRepository.findById(userId)).willReturn(Optional.of(users));
    given(reviewRepository.existsByBookIdAndUserId(bookId, userId)).willReturn(true);

    ReviewCreateRequest request = new ReviewCreateRequest(userId, bookId, "내용", 5);

    assertThatThrownBy(() -> reviewService.createReview(request))
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
  @DisplayName("리뷰 목록 조회 성공 테스트")
  @SuppressWarnings("unchecked")
  void getReviews_success() {
    UUID currentUserId = UUID.randomUUID();
    ReviewListRequest request = new ReviewListRequest(
        null, null, null, 10, null, null, "LATEST", Sort.Direction.DESC
    );

    Slice<ReviewDto> mockSlice = mock(Slice.class);
    CursorPageResponseDto<ReviewDto> mockResponse = mock(CursorPageResponseDto.class);

    given(reviewRepository.searchReviews(eq(request), eq(currentUserId))).willReturn(mockSlice);
    doReturn(mockResponse).when(cursorPageResponseMapper)
        .fromSlice(any(), any(), any(), any(), any());

    CursorPageResponseDto<ReviewDto> result = reviewService.getReviews(request, currentUserId);

    assertThat(result).isNotNull();
    verify(reviewRepository, times(1)).searchReviews(eq(request), eq(currentUserId));
  }

  @Test
  @DisplayName("리뷰 좋아요 성공 테스트 - 기존에 누른 적이 없으면 [추가]된다")
  void toggleLike_addLike() {
    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
    given(userRepository.findById(userId)).willReturn(Optional.of(users));
    given(reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId)).willReturn(
        Optional.empty());
    given(review.getLikeCount()).willReturn(1);

    ReviewLikeDto result = reviewService.toggleLike(reviewId, userId);

    verify(reviewLikeRepository, times(1)).save(any(ReviewLike.class));
    verify(review, times(1)).addLikeCount();
    assertThat(result.liked()).isTrue();
    assertThat(result.likeCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("인기 리뷰 조회 성공 테스트")
  @SuppressWarnings("unchecked")
  void getPopularReviews_success() {
    Page<Review> mockPage = mock(Page.class);
    given(mockPage.getContent()).willReturn(List.of(review));
    given(reviewRepository.findAll(any(PageRequest.class))).willReturn(mockPage);

    ReviewDto mockReviewDto = mock(ReviewDto.class);
    given(bookRepository.findById(any())).willReturn(Optional.of(book));
    given(userRepository.findById(any())).willReturn(Optional.of(users));
    given(reviewMapper.toDto(any(), any(), any(), anyBoolean())).willReturn(mockReviewDto);

    List<ReviewDto> result = reviewService.getPopularReviews();

    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("리뷰 상세 조회 성공 테스트")
  void getReviewDetail_success() {
    ReviewDto mockReviewDto = mock(ReviewDto.class);

    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
    given(userRepository.findById(userId)).willReturn(Optional.of(users));
    given(reviewLikeRepository.existsByReviewIdAndUserId(reviewId, userId)).willReturn(true);
    given(reviewMapper.toDto(review, book, users, true)).willReturn(mockReviewDto);

    ReviewDto result = reviewService.getReviewDetail(reviewId, userId);

    assertThat(result).isNotNull();
    verify(reviewRepository, times(1)).findById(reviewId);
  }

  @Test
  @DisplayName("리뷰 완전 삭제(하드 삭제) 성공 테스트")
  void hardDeleteReview_success() {
    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

    reviewService.hardDeleteReview(reviewId, userId);

    verify(reviewRepository, times(1)).delete(review);
  }
}