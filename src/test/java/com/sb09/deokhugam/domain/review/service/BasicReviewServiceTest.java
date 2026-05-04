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
import com.sb09.deokhugam.domain.dashboard.entity.PeriodType;
import com.sb09.deokhugam.domain.dashboard.entity.PopularReview;
import com.sb09.deokhugam.domain.dashboard.repository.PopularReviewRepository;
import com.sb09.deokhugam.domain.notification.entity.NotificationType;
import com.sb09.deokhugam.domain.notification.service.NotificationService;
import com.sb09.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sb09.deokhugam.domain.review.dto.request.ReviewListRequest;
import com.sb09.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.sb09.deokhugam.domain.review.dto.response.PopularReviewDto;
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

import com.sb09.deokhugam.global.exception.CustomException;
import com.sb09.deokhugam.global.exception.ErrorCode;
import com.sb09.deokhugam.global.exception.review.DuplicateReviewException;
import com.sb09.deokhugam.global.exception.review.ReviewAlreadyDeletedException;
import com.sb09.deokhugam.global.exception.review.ReviewForbiddenException;
import com.sb09.deokhugam.global.exception.review.ReviewNotFoundException;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import com.sb09.deokhugam.global.common.mapper.CursorPageResponseMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
  private NotificationService notificationService;
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
  @Mock
  private PopularReviewRepository popularReviewRepository;

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
    given(book.getDeletedAt()).willReturn(null);

    given(users.getId()).willReturn(userId);
    given(users.getDeletedAt()).willReturn(null); // 삭제 방어막 통과를 위한 설정

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
    given(reviewRepository.existsByBookIdAndUserIdAndDeletedAtIsNull(bookId, userId)).willReturn(
        false);
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
    given(reviewRepository.existsByBookIdAndUserIdAndDeletedAtIsNull(bookId, userId)).willReturn(
        true);

    ReviewCreateRequest request = new ReviewCreateRequest(userId, bookId, "내용", 5);

    assertThatThrownBy(() -> reviewService.createReview(request))
        .isInstanceOf(DuplicateReviewException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.DUPLICATE_REVIEW));
  }

  @Test
  @DisplayName("예외 검증 - 삭제된 리뷰를 수정하려고 하면 예외 발생")
  void updateReview_alreadyDeleted() {
    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
    given(review.getDeletedAt()).willReturn(LocalDateTime.now()); // 리뷰가 삭제된 상태 재현

    ReviewUpdateRequest request = new ReviewUpdateRequest("수정내용", 4);

    assertThatThrownBy(() -> reviewService.updateReview(reviewId, request, userId))
        .isInstanceOf(CustomException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.REVIEW_NOT_FOUND));
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
  @DisplayName("리뷰 좋아요 성공 테스트 - 기존에 누른 적이 없으면 [추가]되고 알림 발송")
  void toggleLike_addLike() {
    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
    given(userRepository.findById(userId)).willReturn(Optional.of(users));
    given(reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId)).willReturn(
        Optional.empty());
    given(review.getLikeCount()).willReturn(1);

    ReviewLikeDto result = reviewService.toggleLike(reviewId, userId);

    verify(reviewLikeRepository, times(1)).save(any(ReviewLike.class));
    verify(review, times(1)).addLikeCount();
    //  알림 발송 로직이 제대로 실행되었는지 검증
    verify(notificationService, times(1)).create(any(NotificationType.class), eq(review),
        eq(users));

    assertThat(result.liked()).isTrue();
    assertThat(result.likeCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("인기 리뷰 조회 성공 테스트 - 전체 기간(ALL)")
  void getPopularReviews_success() {
    PopularReview pr = mock(PopularReview.class);
    given(pr.getPeriod()).willReturn(PeriodType.ALL_TIME);
    given(pr.getBaseDate()).willReturn(LocalDate.now());
    given(pr.getRanking()).willReturn(1L);
    given(pr.getReviewId()).willReturn(reviewId);

    given(
        popularReviewRepository.findTopByPeriodOrderByBaseDateDesc(PeriodType.ALL_TIME)).willReturn(
        Optional.of(pr));
    given(popularReviewRepository.findAll()).willReturn(List.of(pr));
    given(reviewRepository.findAllById(List.of(reviewId))).willReturn(List.of(review));

    ReviewDto mockReviewDto = mock(ReviewDto.class);
    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
    given(userRepository.findById(userId)).willReturn(Optional.of(users));
    given(reviewMapper.toDto(eq(review), eq(book), eq(users), eq(false))).willReturn(mockReviewDto);

    List<PopularReviewDto> result = reviewService.getPopularReviews("ALL");

    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("인기 리뷰 조회 성공 테스트 - 특정 기간(DAILY)")
  void getPopularReviews_daily_success() {
    PopularReview pr = mock(PopularReview.class);
    given(pr.getPeriod()).willReturn(PeriodType.DAILY);
    given(pr.getBaseDate()).willReturn(LocalDate.now());
    given(pr.getRanking()).willReturn(1L);
    given(pr.getReviewId()).willReturn(reviewId);
    given(pr.getLikeCount()).willReturn(10L);
    given(pr.getCommentCount()).willReturn(5L);

    given(popularReviewRepository.findTopByPeriodOrderByBaseDateDesc(PeriodType.DAILY)).willReturn(
        Optional.of(pr));
    given(popularReviewRepository.findAll()).willReturn(List.of(pr));
    given(reviewRepository.findAllById(List.of(reviewId))).willReturn(List.of(review));

    // N+1 최적화로 인해 findById -> findAllById 로 Mocking 변경
    given(bookRepository.findAllById(List.of(bookId))).willReturn(List.of(book));
    given(userRepository.findAllById(List.of(userId))).willReturn(List.of(users));

    // Mapper 로직 제거 (직접 DTO를 생성하므로 더 이상 필요 없음)

    List<PopularReviewDto> result = reviewService.getPopularReviews("DAILY");

    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    assertThat(result.get(0).likeCount()).isEqualTo(10L); // 💡 값이 제대로 들어갔는지 검증 추가
  }

  @Test
  @DisplayName("인기 리뷰 조회 성공 테스트 - 특정 기간(WEEKLY)")
  void getPopularReviews_weekly_success() {
    PopularReview pr = mock(PopularReview.class);
    given(pr.getPeriod()).willReturn(PeriodType.WEEKLY);
    given(pr.getBaseDate()).willReturn(LocalDate.now());
    given(pr.getRanking()).willReturn(1L);
    given(pr.getReviewId()).willReturn(reviewId);
    given(pr.getLikeCount()).willReturn(20L);
    given(pr.getCommentCount()).willReturn(10L);

    given(popularReviewRepository.findTopByPeriodOrderByBaseDateDesc(PeriodType.WEEKLY)).willReturn(
        Optional.of(pr));
    given(popularReviewRepository.findAll()).willReturn(List.of(pr));
    given(reviewRepository.findAllById(List.of(reviewId))).willReturn(List.of(review));

    given(bookRepository.findAllById(List.of(bookId))).willReturn(List.of(book));
    given(userRepository.findAllById(List.of(userId))).willReturn(List.of(users));

    List<PopularReviewDto> result = reviewService.getPopularReviews("WEEKLY");

    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("인기 리뷰 조회 성공 테스트 - 특정 기간(MONTHLY)")
  void getPopularReviews_monthly_success() {
    PopularReview pr = mock(PopularReview.class);
    given(pr.getPeriod()).willReturn(PeriodType.MONTHLY);
    given(pr.getBaseDate()).willReturn(LocalDate.now());
    given(pr.getRanking()).willReturn(1L);
    given(pr.getReviewId()).willReturn(reviewId);
    given(pr.getLikeCount()).willReturn(30L);
    given(pr.getCommentCount()).willReturn(15L);

    given(
        popularReviewRepository.findTopByPeriodOrderByBaseDateDesc(PeriodType.MONTHLY)).willReturn(
        Optional.of(pr));
    given(popularReviewRepository.findAll()).willReturn(List.of(pr));
    given(reviewRepository.findAllById(List.of(reviewId))).willReturn(List.of(review));

    given(bookRepository.findAllById(List.of(bookId))).willReturn(List.of(book));
    given(userRepository.findAllById(List.of(userId))).willReturn(List.of(users));

    List<PopularReviewDto> result = reviewService.getPopularReviews("MONTHLY");

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

  @Test
  @DisplayName("예외 검증 - 본인이 아닌 다른 유저가 리뷰 삭제 시도 시 예외 발생")
  void deleteReview_notOwner() {
    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
    UUID otherUserId = UUID.randomUUID();

    assertThatThrownBy(() -> reviewService.deleteReview(reviewId, otherUserId))
        .isInstanceOf(ReviewForbiddenException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.UNAUTHORIZED_ACCESS));
  }

  @Test
  @DisplayName("예외 검증 - 이미 논리삭제된 리뷰를 또 삭제 시도 시 예외 발생")
  void deleteReview_alreadyDeleted() {
    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
    given(review.getUserId()).willReturn(userId);
    given(review.getDeletedAt()).willReturn(LocalDateTime.now());

    assertThatThrownBy(() -> reviewService.deleteReview(reviewId, userId))
        .isInstanceOf(ReviewAlreadyDeletedException.class);
  }

  @Test
  @DisplayName("리뷰 삭제(논리 삭제) 성공 및 도서 통계 차감 테스트")
  void deleteReview_success() {
    // given: 정상적인 상태 셋팅
    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
    given(review.getUserId()).willReturn(userId); // 권한 통과
    given(review.getDeletedAt()).willReturn(null); // 아직 삭제 안 됨 통과

    // 도서 평점 깎는 로직(removeBookStats)을 위해 필요한 셋팅
    given(review.getBookId()).willReturn(bookId);
    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
    given(review.getRating()).willReturn(5);

    // when: 삭제 요청
    reviewService.deleteReview(reviewId, userId);

    // then: 삭제 처리(markAsDeleted)가 잘 호출되었는지, 도서 조회도 잘 일어났는지 검증
    verify(review, times(1)).markAsDeleted();
    verify(bookRepository, times(1)).findById(bookId);
  }
  // ==========================================
  //  리뷰 등록 (Create) 추가 예외 테스트
  // ==========================================

  @Test
  @DisplayName("예외 검증 - 존재하지 않는 도서에 리뷰 등록 시도")
  void createReview_bookNotFound() {
    given(bookRepository.findById(bookId)).willReturn(Optional.empty());
    ReviewCreateRequest request = new ReviewCreateRequest(userId, bookId, "내용", 5);

    assertThatThrownBy(() -> reviewService.createReview(request))
        .isInstanceOf(CustomException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.BOOK_NOT_FOUND));
  }

  @Test
  @DisplayName("예외 검증 - 삭제된(휴지통) 도서에 리뷰 등록 시도")
  void createReview_bookAlreadyDeleted() {
    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
    given(book.getDeletedAt()).willReturn(LocalDateTime.now()); // 도서 삭제 상태 재현
    ReviewCreateRequest request = new ReviewCreateRequest(userId, bookId, "내용", 5);

    assertThatThrownBy(() -> reviewService.createReview(request))
        .isInstanceOf(CustomException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.BOOK_NOT_FOUND));
  }

  @Test
  @DisplayName("예외 검증 - 존재하지 않는 사용자가 리뷰 등록 시도")
  void createReview_userNotFound() {
    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
    given(userRepository.findById(userId)).willReturn(Optional.empty());
    ReviewCreateRequest request = new ReviewCreateRequest(userId, bookId, "내용", 5);

    assertThatThrownBy(() -> reviewService.createReview(request))
        .isInstanceOf(CustomException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.USER_NOT_FOUND));
  }

  @Test
  @DisplayName("예외 검증 - 탈퇴한(삭제된) 사용자가 리뷰 등록 시도")
  void createReview_userAlreadyDeleted() {
    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
    given(userRepository.findById(userId)).willReturn(Optional.of(users));
    given(users.getDeletedAt()).willReturn(LocalDateTime.now()); // 사용자 탈퇴 상태 재현
    ReviewCreateRequest request = new ReviewCreateRequest(userId, bookId, "내용", 5);

    assertThatThrownBy(() -> reviewService.createReview(request))
        .isInstanceOf(CustomException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.USER_NOT_FOUND));
  }

  // ==========================================
  //  리뷰 좋아요 (Like) 추가 테스트
  // ==========================================

  @Test
  @DisplayName("리뷰 좋아요 성공 테스트 - 이미 누른 상태면 [취소]됨")
  void toggleLike_removeLike() {
    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
    given(userRepository.findById(userId)).willReturn(Optional.of(users));

    ReviewLike existingLike = mock(ReviewLike.class);
    // 이미 좋아요가 존재하는 상황 재현
    given(reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId)).willReturn(
        Optional.of(existingLike));
    given(review.getLikeCount()).willReturn(0);

    ReviewLikeDto result = reviewService.toggleLike(reviewId, userId);

    verify(reviewLikeRepository, times(1)).delete(existingLike);
    verify(review, times(1)).removeLikeCount(); // 좋아요 감소 로직 호출 확인

    assertThat(result.liked()).isFalse();
  }

  @Test
  @DisplayName("예외 검증 - 이미 삭제된 리뷰에 좋아요 시도 시 예외 발생")
  void toggleLike_deletedReview() {
    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
    given(review.getDeletedAt()).willReturn(LocalDateTime.now()); // 리뷰 삭제 상태

    assertThatThrownBy(() -> reviewService.toggleLike(reviewId, userId))
        .isInstanceOf(CustomException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.REVIEW_NOT_FOUND));
  }

  @Test
  @DisplayName("예외 검증 - 없는 사용자가 좋아요 시도 시 예외 발생")
  void toggleLike_userNotFound() {
    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> reviewService.toggleLike(reviewId, userId))
        .isInstanceOf(CustomException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.USER_NOT_FOUND));
  }

  // ==========================================
  //  리뷰 단건 조회 (Detail) 추가 예외 테스트
  // ==========================================

  @Test
  @DisplayName("예외 검증 - 존재하지 않는 리뷰 상세 조회 시도")
  void getReviewDetail_notFound() {
    given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> reviewService.getReviewDetail(reviewId, userId))
        .isInstanceOf(ReviewNotFoundException.class);
  }

  @Test
  @DisplayName("예외 검증 - 삭제된 리뷰 상세 조회 시도 시 예외 발생")
  void getReviewDetail_deletedReview() {
    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
    given(review.getDeletedAt()).willReturn(LocalDateTime.now()); // 리뷰 삭제 상태

    assertThatThrownBy(() -> reviewService.getReviewDetail(reviewId, userId))
        .isInstanceOf(CustomException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.REVIEW_NOT_FOUND));
  }

  // ==========================================
  //  리뷰 등록  - 입력값 검증 3종
  // ==========================================

  @Test
  @DisplayName("예외 검증 - 리뷰 등록 시 필수값 누락 (평점 또는 내용 없음)")
  void createReview_missingRequiredFields() {
    ReviewCreateRequest request = new ReviewCreateRequest(userId, bookId, null,
        null); // 널(null) 값 전달
    assertThatThrownBy(() -> reviewService.createReview(request))
        .isInstanceOf(RuntimeException.class); // 현하님이 만드신 예외가 터져야 성공!
  }

  @Test
  @DisplayName("예외 검증 - 리뷰 등록 시 평점 범위 오류 (1~5점 이탈)")
  void createReview_invalidRatingRange() {
    ReviewCreateRequest request = new ReviewCreateRequest(userId, bookId, "정상적인 내용입니다.",
        6); // 6점 전달
    assertThatThrownBy(() -> reviewService.createReview(request))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("예외 검증 - 리뷰 등록 시 내용 길이 오류 (너무 짧음)")
  void createReview_invalidContentLength() {
    ReviewCreateRequest request = new ReviewCreateRequest(userId, bookId, "", 5); // 빈 문자열 전달
    assertThatThrownBy(() -> reviewService.createReview(request))
        .isInstanceOf(RuntimeException.class);
  }

  // ==========================================
  //  리뷰 수정  - 입력값 검증 3종
  // ==========================================

  @Test
  @DisplayName("예외 검증 - 리뷰 수정 시 필수값 누락")
  void updateReview_missingRequiredFields() {
    ReviewUpdateRequest request = new ReviewUpdateRequest(null, null);
    assertThatThrownBy(() -> reviewService.updateReview(reviewId, request, userId))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("예외 검증 - 리뷰 수정 시 평점 범위 오류")
  void updateReview_invalidRatingRange() {
    ReviewUpdateRequest request = new ReviewUpdateRequest("정상적인 내용입니다.", 0); // 0점 전달
    assertThatThrownBy(() -> reviewService.updateReview(reviewId, request, userId))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("예외 검증 - 리뷰 수정 시 내용 길이 오류")
  void updateReview_invalidContentLength() {
    ReviewUpdateRequest request = new ReviewUpdateRequest("", 5);
    assertThatThrownBy(() -> reviewService.updateReview(reviewId, request, userId))
        .isInstanceOf(RuntimeException.class);
  }

  // ==========================================
  // 리뷰 목록 조회 - 목록 및 필터 검증
  // ==========================================

  @Test
  @DisplayName("예외 검증 - 목록 조회 시 잘못된 정렬 조건")
  void getReviews_invalidSortCondition() {
    // orderBy에 "STRANGE_SORT" 같이 약속되지 않은 값 전달
    ReviewListRequest request = new ReviewListRequest(null, null, null, 10, null, null,
        "STRANGE_SORT", Sort.Direction.DESC);

    assertThatThrownBy(() -> reviewService.getReviews(request, userId))
        .isInstanceOf(Exception.class);
  }

  @Test
  @DisplayName("예외 검증 - 목록 조회 시 limit 범위 오류 (음수)")
  void getReviews_invalidLimitBounds() {
    ReviewListRequest request = new ReviewListRequest(null, null, null, -5, null, null, "LATEST",
        Sort.Direction.DESC);

    assertThatThrownBy(() -> reviewService.getReviews(request, userId))
        .isInstanceOf(Exception.class);
  }

  @Test
  @DisplayName("예외 검증 - 존재하지 않는 작성자 ID로 필터링 시도")
  void getReviews_authorNotFound() {
    UUID fakeAuthorId = UUID.randomUUID();
    ReviewListRequest request = new ReviewListRequest(null, fakeAuthorId, null, 10, null, null,
        "LATEST", Sort.Direction.DESC);

    // Repository가 빈 값을 반환하도록 모킹
    given(userRepository.findById(fakeAuthorId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> reviewService.getReviews(request, userId))
        .isInstanceOf(Exception.class);
  }

  @Test
  @DisplayName("성공 테스트 - 헤더 userId 누락(비로그인) 시 정상 조회 및 likedByMe false 처리")
  @SuppressWarnings("unchecked")
  void getReviews_noHeaderUserId() {
    ReviewListRequest request = new ReviewListRequest(null, null, null, 10, null, null, "LATEST",
        Sort.Direction.DESC);
    Slice<ReviewDto> mockSlice = mock(Slice.class);
    CursorPageResponseDto<ReviewDto> mockResponse = mock(CursorPageResponseDto.class);

    // currentUserId 자리에 null이 들어감
    given(reviewRepository.searchReviews(eq(request), eq(null))).willReturn(mockSlice);
    doReturn(mockResponse).when(cursorPageResponseMapper)
        .fromSlice(any(), any(), any(), any(), any());

    CursorPageResponseDto<ReviewDto> result = reviewService.getReviews(request, null);

    assertThat(result).isNotNull();
    verify(reviewRepository, times(1)).searchReviews(eq(request), eq(null));
  }

  @Test
  @DisplayName("예외 검증 - 리뷰 등록 시 내용이 null인 경우 (부분 커버리지 보완)")
  void createReview_contentIsNull() {
    ReviewCreateRequest request = new ReviewCreateRequest(userId, bookId, null, 5); // 내용만 null
    assertThatThrownBy(() -> reviewService.createReview(request))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("예외 검증 - 리뷰 등록 시 내용이 띄어쓰기(공백)만 있는 경우 (부분 커버리지 보완)")
  void createReview_contentIsBlank() {
    ReviewCreateRequest request = new ReviewCreateRequest(userId, bookId, "   ", 5); // 공백만 전달
    assertThatThrownBy(() -> reviewService.createReview(request))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("예외 검증 - 리뷰 등록 시 평점이 5점을 초과하는 경우 (부분 커버리지 보완)")
  void createReview_ratingTooHigh() {
    ReviewCreateRequest request = new ReviewCreateRequest(userId, bookId, "정상적인 내용", 6); // 6점 전달
    assertThatThrownBy(() -> reviewService.createReview(request))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("예외 검증 - 리뷰 등록 시 내용이 1000자를 초과하는 경우 (부분 커버리지 보완)")
  void createReview_contentTooLong() {
    String longContent = "a".repeat(1001); // 'a'를 1001번 반복해서 아주 긴 텍스트 생성
    ReviewCreateRequest request = new ReviewCreateRequest(userId, bookId, longContent, 5);
    assertThatThrownBy(() -> reviewService.createReview(request))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("예외 검증 - 목록 조회 시 존재하지 않는 도서 ID로 필터링 시도")
  void getReviews_bookNotFound() {
    UUID fakeBookId = UUID.randomUUID();
    ReviewListRequest request = new ReviewListRequest(fakeBookId, null, null, 10, null, null,
        "LATEST", Sort.Direction.DESC);

    // Repository가 빈 값을 반환하도록 모킹
    given(bookRepository.findById(fakeBookId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> reviewService.getReviews(request, userId))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("성공 테스트 - 목록 조회 시 정렬 조건이 RATING 인 경우 (부분 커버리지 보완)")
  @SuppressWarnings("unchecked")
  void getReviews_orderByRating() {
    ReviewListRequest request = new ReviewListRequest(null, null, null, 10, null, null, "RATING",
        Sort.Direction.DESC);
    Slice<ReviewDto> mockSlice = mock(Slice.class);
    CursorPageResponseDto<ReviewDto> mockResponse = mock(CursorPageResponseDto.class);

    given(reviewRepository.searchReviews(eq(request), eq(userId))).willReturn(mockSlice);
    doReturn(mockResponse).when(cursorPageResponseMapper)
        .fromSlice(any(), any(), any(), any(), any());

    CursorPageResponseDto<ReviewDto> result = reviewService.getReviews(request, userId);

    assertThat(result).isNotNull();
  }

  @Test
  @DisplayName("보충 1: 도서의 [마지막 남은 리뷰 1개]를 삭제할 때의 통계 차감 로직 검증")
  void deleteReview_lastReview() {
    // given (상황 세팅: 리뷰가 1개만 있는 도서)
    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
    given(review.getUserId()).willReturn(userId);
    given(review.getDeletedAt()).willReturn(null);
    given(review.getBookId()).willReturn(bookId);
    given(review.getRating()).willReturn(5);

    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
    given(book.getReviewCount()).willReturn(1); // 리뷰가 1개! (이거 삭제하면 0개가 됨)

    // when
    reviewService.deleteReview(reviewId, userId);

    // then
    verify(review, times(1)).markAsDeleted();
    verify(book, times(1)).updateRatingAndReviewCount(any(), eq(0)); // 0개로 업데이트 되었는지 확인
  }

  @Test
  @DisplayName("보충 2: 리뷰 삭제 시, 도서가 이미 DB에서 지워져서 없는(null) 경우의 방어 로직 검증")
  void deleteReview_bookIsNull() {
    // given
    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
    given(review.getUserId()).willReturn(userId);
    given(review.getDeletedAt()).willReturn(null);
    given(review.getBookId()).willReturn(bookId);

    // 도서를 찾았는데 없는 상황 (Optional.empty 반환)
    given(bookRepository.findById(bookId)).willReturn(Optional.empty());

    // when
    reviewService.deleteReview(reviewId, userId);

    // then
    verify(review, times(1)).markAsDeleted();
    // 도서가 없으니 통계 업데이트 로직은 실행되지 않고 무사히 넘어가야 함!
  }

  @Test
  @DisplayName("보충 3: 비로그인(currentUserId == null) 사용자가 리뷰 상세 조회를 요청할 때")
  void getReviewDetail_notLoggedIn() {
    // given
    ReviewDto mockReviewDto = mock(ReviewDto.class);
    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
    given(review.getDeletedAt()).willReturn(null);
    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
    given(userRepository.findById(userId)).willReturn(Optional.of(users));

    // likedByMe가 false로 잘 들어가는지 확인
    given(reviewMapper.toDto(review, book, users, false)).willReturn(mockReviewDto);

    // when (요청자 ID 자리에 null 전달)
    ReviewDto result = reviewService.getReviewDetail(reviewId, null);

    // then
    assertThat(result).isNotNull();
    // 비로그인이므로 좋아요 여부 DB 조회를 아예 하지 말아야 함!
    verify(reviewLikeRepository, org.mockito.Mockito.never()).existsByReviewIdAndUserId(any(),
        any());
  }

  @Test
  @DisplayName("보충 4: 관리자용 완전 삭제(hardDelete) 시 존재하지 않는 리뷰 ID를 넣었을 때")
  void hardDeleteReview_notFound() {
    // given
    given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> reviewService.hardDeleteReview(reviewId, userId))
        .isInstanceOf(ReviewNotFoundException.class);
  }

  @Test
  @DisplayName("인기 리뷰 조회 - 대시보드에 집계된 데이터가 없을 경우 빈 리스트 반환 (Coverage 보완)")
  void getPopularReviews_emptyList() {
    // given: DB에 저장된 인기 리뷰가 아예 없는 상황
    given(popularReviewRepository.findTopByPeriodOrderByBaseDateDesc(PeriodType.DAILY))
        .willReturn(Optional.empty());
    given(popularReviewRepository.findAll()).willReturn(List.of());

    // when
    List<PopularReviewDto> result = reviewService.getPopularReviews("DAILY");

    // then: isEmpty()에 걸려 List.of()가 반환되어야 함
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("인기 리뷰 조회 - 대시보드 데이터는 있으나 원본 리뷰가 삭제된 경우 무시 (Coverage 보완)")
  void getPopularReviews_reviewIsNull() {
    // given: 대시보드에는 랭킹 데이터가 있음
    PopularReview pr = mock(PopularReview.class);
    given(pr.getPeriod()).willReturn(PeriodType.DAILY);
    given(pr.getBaseDate()).willReturn(LocalDate.now());
    given(pr.getRanking()).willReturn(1L);
    given(pr.getReviewId()).willReturn(reviewId);

    given(popularReviewRepository.findTopByPeriodOrderByBaseDateDesc(PeriodType.DAILY))
        .willReturn(Optional.of(pr));
    given(popularReviewRepository.findAll()).willReturn(List.of(pr));

    // 핵심: 원본 리뷰 저장소에서는 해당 리뷰를 못 찾음 (빈 리스트 반환) -> review == null 상황 유도
    given(reviewRepository.findAllById(List.of(reviewId))).willReturn(List.of());

    // when
    List<PopularReviewDto> result = reviewService.getPopularReviews("DAILY");

    // then: null로 처리되어 .filter(Objects::nonNull)에서 걸러지므로 최종 결과는 빈 리스트여야 함
    assertThat(result).isEmpty();
  }

}