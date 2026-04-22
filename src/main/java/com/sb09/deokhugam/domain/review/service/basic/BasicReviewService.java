package com.sb09.deokhugam.domain.review.service.basic;

import com.sb09.deokhugam.domain.book.entity.Book;
import com.sb09.deokhugam.domain.book.repository.BookRepository;
import com.sb09.deokhugam.domain.notification.entity.NotificationType;
import com.sb09.deokhugam.domain.notification.service.NotificationService;
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
import com.sb09.deokhugam.domain.review.service.ReviewService;
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

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicReviewService implements ReviewService {

  private final NotificationService notificationService;
  private final ReviewRepository reviewRepository;
  private final BookRepository bookRepository;
  private final UserRepository userRepository;
  private final ReviewLikeRepository reviewLikeRepository;
  private final ReviewMapper reviewMapper;
  private final CursorPageResponseMapper cursorPageResponseMapper;

  /**
   * 1. 리뷰 등록 (Create)
   */
  @Override
  @Transactional
  public ReviewDto createReview(ReviewCreateRequest request) { // 반환 타입 변경

    UUID userId = request.userId();
    // 도서와 유저가 실제로 DB에 존재하는지 확인 (타 도메인이므로 기존 CustomException 유지)
    Book book = bookRepository.findById(request.bookId())
        .orElseThrow(() -> {
          log.warn("도서를 찾을 수 없습니다. bookId: {}", request.bookId());
          return new CustomException(ErrorCode.BOOK_NOT_FOUND);
        });

    Users user = userRepository.findById(userId)
        .orElseThrow(() -> {
          log.warn("사용자를 찾을 수 없습니다. userId: {}", userId);
          return new CustomException(ErrorCode.USER_NOT_FOUND);
        });

    // 중복 작성 검증 (1인 1리뷰)
    if (reviewRepository.existsByBookIdAndUserIdAndDeletedAtIsNull(book.getId(), user.getId())) {
      log.warn("이미 리뷰를 작성한 사용자입니다. bookId: {}, userId: {}", book.getId(), user.getId());
      throw new DuplicateReviewException();
    }

    // 리뷰 엔티티 생성 및 저장
    Review review = Review.builder()
        .bookId(book.getId())
        .userId(user.getId())
        .content(request.content())
        .rating(request.rating())
        .build();

    Review savedReview = reviewRepository.save(review);

    // 도서 통계 업데이트 로직 호출
    updateBookStats(book, request.rating());

    log.info("리뷰가 성공적으로 등록되었습니다. reviewId: {}, userId: {}", savedReview.getId(), user.getId());

    // 저장된 리뷰를 Dto로 매핑하여 반환
    return reviewMapper.toDto(savedReview, book, user, false);
  }

  /**
   * 2. 리뷰 수정 (Update) - 리뷰 내용 및 평점 변경 (JPA 더티 체킹 활용)
   */
  @Override
  @Transactional
  public ReviewDto updateReview(UUID reviewId, ReviewUpdateRequest request,
      UUID userId) { // 반환 타입 변경

    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> {
          log.warn("리뷰를 찾을 수 없습니다. reviewId: {}", reviewId);
          return ReviewNotFoundException.withId(reviewId);
        }); // ID 추적 기능(withId) 적용

    // 권한 확인: 수정을 요청한 사람이 실제 작성자인지 검사 (CustomException 추가)
    if (!review.getUserId().equals(userId)) {
      log.warn("리뷰 수정 권한이 없습니다. reviewId: {}, 요청 userId: {}", reviewId, userId);
      throw new ReviewForbiddenException();
    }

    // 내용 및 평점 업데이트
    review.updateReview(request.content(), request.rating());

    log.info("리뷰가 성공적으로 수정되었습니다. reviewId: {}", reviewId);

    // 수정한 리뷰를 Dto로 반환하기 위해 타 도메인 정보 조회
    Book book = bookRepository.findById(review.getBookId()).orElse(null);
    Users user = userRepository.findById(review.getUserId()).orElse(null);
    boolean likedByMe = reviewLikeRepository.existsByReviewIdAndUserId(review.getId(), userId);

    return reviewMapper.toDto(review, book, user, likedByMe);
  }

  /**
   * 3. 리뷰 삭제 (Delete - 논리 삭제) - 작성자 본인인지 권한 확인 - deleted_at 필드에 현재 시간을 찍어 논리적 삭제 처리
   */
  @Override
  @Transactional
  public void deleteReview(UUID reviewId, UUID userId) {

    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> {
          log.warn("리뷰를 찾을 수 없습니다. reviewId: {}", reviewId);
          return ReviewNotFoundException.withId(reviewId);
        });

    // 권한 확인
    if (!review.getUserId().equals(userId)) {
      log.warn("리뷰 삭제 권한이 없습니다. reviewId: {}, 요청 userId: {}", reviewId, userId);
      throw new ReviewForbiddenException();
    }

    // 이미 삭제된 리뷰인지 확인
    if (review.getDeletedAt() != null) {
      log.warn("이미 삭제된 리뷰입니다. reviewId: {}", reviewId);
      throw ReviewAlreadyDeletedException.withId(reviewId);
    }

    // 물리 삭제 대신 BaseFullAuditEntity에서 제공하는 메서드로 논리 삭제 처리
    review.markAsDeleted();

    // ---- 추가  ----
    // 해당 도서를 찾아와서 통계(평점, 리뷰 수)를 차감합니다!
    Book book = bookRepository.findById(review.getBookId()).orElse(null);
    if (book != null) {
      removeBookStats(book, review.getRating());
    }
    log.info("리뷰가 성공적으로 삭제 처리되었습니다(논리 삭제). reviewId: {}", reviewId);
  }

  /**
   * 4. 리뷰 목록 조회 (무한 스크롤 및 검색)
   */
  @Override
  public CursorPageResponseDto<ReviewDto> getReviews(ReviewListRequest request,
      UUID currentUserId) {

    log.info("리뷰 목록 조회를 요청했습니다. 요청자 userId: {}", currentUserId);

    // 이제 파라미터로 currentUserId 도 같이 넘겨주어야 합니다
    Slice<ReviewDto> reviewDtos = reviewRepository.searchReviews(request, currentUserId);

    Long totalElements = 0L; // 무한 스크롤이라 전체 개수는 임시로 0 처리

    return cursorPageResponseMapper.fromSlice(
        reviewDtos,
        dto -> dto,
        ReviewDto::id,
        ReviewDto::createdAt,
        totalElements
    );
  }

  /**
   * 5. 리뷰 좋아요 토글 (Toggle) - 이미 좋아요를 눌렀다면 취소, 누르지 않았다면 추가합니다.
   */
  @Override
  @Transactional
  public ReviewLikeDto toggleLike(UUID reviewId, UUID userId) {

    //  리뷰와 유저가 존재하는지 확인
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> {
          log.warn("좋아요 실패: 리뷰를 찾을 수 없습니다. reviewId: {}", reviewId);
          return ReviewNotFoundException.withId(reviewId);
        });

    Users user = userRepository.findById(userId)
        .orElseThrow(() -> {
          log.warn("좋아요 실패: 사용자를 찾을 수 없습니다. userId: {}", userId);
          return new CustomException(ErrorCode.USER_NOT_FOUND);
        });

    // 이미 좋아요를 누른 기록이 있는지 조회
    Optional<ReviewLike> existingLike = reviewLikeRepository.findByReviewIdAndUserId(reviewId,
        userId);

    if (existingLike.isPresent()) {
      // [좋아요 취소] 이미 누른 상태라면 기록을 지우고 카운트를 1 내립니다.
      reviewLikeRepository.delete(existingLike.get());
      review.removeLikeCount();
      log.info("리뷰 좋아요가 취소되었습니다. reviewId: {}, userId: {}", reviewId, userId);

      return new ReviewLikeDto(false, review.getLikeCount());
    } else {
      // [좋아요 추가] 누른 적이 없다면 새로 기록을 만들고 카운트를 1 올립니다.
      ReviewLike newLike = ReviewLike.builder()
          .review(review)
          .user(user)
          .build();
      reviewLikeRepository.save(newLike);
      review.addLikeCount();
      log.info("리뷰 좋아요가 추가되었습니다. reviewId: {}, userId: {}", reviewId, userId);

      notificationService.create(NotificationType.LIKE, review, user);

      return new ReviewLikeDto(true, review.getLikeCount());
    }
  }

  /**
   * 6. 인기 리뷰 조회: 좋아요 많은 순 상위 10개
   */
  @Override
  public java.util.List<ReviewDto> getPopularReviews() {
    org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(
        0, 10,
        org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,
            "likeCount"));
    return reviewRepository.findAll(pageRequest)
        .getContent()
        .stream()
        .map(review -> {
          Book book = bookRepository.findById(review.getBookId()).orElse(null);
          Users user = userRepository.findById(review.getUserId()).orElse(null);
          return reviewMapper.toDto(review, book, user, false);
        })
        .toList();
  }

  /**
   * 7. 리뷰 상세 조회
   */
  @Override
  public ReviewDto getReviewDetail(UUID reviewId, UUID currentUserId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> ReviewNotFoundException.withId(reviewId));

    Book book = bookRepository.findById(review.getBookId()).orElse(null);
    Users user = userRepository.findById(review.getUserId()).orElse(null);
    boolean likedByMe =
        (currentUserId != null) && reviewLikeRepository.existsByReviewIdAndUserId(reviewId,
            currentUserId);

    return reviewMapper.toDto(review, book, user, likedByMe);
  }

  /**
   * 8. 리뷰 물리 삭제 (테스트 및 관리자용)
   */
  @Override
  @Transactional
  public void hardDeleteReview(UUID reviewId, UUID userId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> ReviewNotFoundException.withId(reviewId));

    // 엔티티에 설정된 cascade = CascadeType.ALL 옵션으로 좋아요도 함께 물리 삭제됨
    reviewRepository.delete(review);
  }

  /**
   * [내부 로직] 도서 평균 평점 및 리뷰 수 계산 (리뷰 등록 시 더하기용)
   */
  private void updateBookStats(Book book, Integer newRatingValue) {
    int newReviewCount = book.getReviewCount() + 1;
    double currentTotal = book.getRating().doubleValue() * book.getReviewCount();
    double newAverage = (currentTotal + newRatingValue) / newReviewCount;

    BigDecimal finalRating = BigDecimal.valueOf(newAverage).setScale(2, RoundingMode.HALF_UP);
    book.updateRatingAndReviewCount(finalRating, newReviewCount);

    log.info("도서 통계가 업데이트되었습니다. bookId: {}, 새 평균 평점: {}, 총 리뷰 수: {}", book.getId(), finalRating,
        newReviewCount);
  }

  /**
   * [내부 로직] 도서 평균 평점 및 리뷰 수 계산 (리뷰 삭제 시 차감용)
   */
  private void removeBookStats(Book book, Integer deletedRatingValue) {
    int newReviewCount = book.getReviewCount() - 1;
    double newAverage = 0.0;

    // 리뷰가 아직 남아있다면 평점 다시 계산
    if (newReviewCount > 0) {
      double currentTotal = book.getRating().doubleValue() * book.getReviewCount();
      newAverage = (currentTotal - deletedRatingValue) / newReviewCount;
    }

    BigDecimal finalRating = BigDecimal.valueOf(newAverage).setScale(2, RoundingMode.HALF_UP);
    book.updateRatingAndReviewCount(finalRating, newReviewCount);

    log.info("도서 통계가 업데이트되었습니다(삭제 반영). bookId: {}, 새 평균 평점: {}, 총 리뷰 수: {}", book.getId(),
        finalRating, newReviewCount);
  }
}