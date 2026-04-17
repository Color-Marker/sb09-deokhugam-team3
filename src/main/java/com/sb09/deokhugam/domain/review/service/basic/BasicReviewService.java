package com.sb09.deokhugam.domain.review.service.basic;

import com.sb09.deokhugam.domain.book.entity.Book;
import com.sb09.deokhugam.domain.book.repository.BookRepository;
import com.sb09.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sb09.deokhugam.domain.review.dto.request.ReviewListRequest;
import com.sb09.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.sb09.deokhugam.domain.review.dto.response.ReviewDto;
import com.sb09.deokhugam.domain.review.entity.Review;
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

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicReviewService implements ReviewService {

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
  public void createReview(ReviewCreateRequest request, UUID userId) {

    // 도서와 유저가 실제로 DB에 존재하는지 확인 (타 도메인이므로 기존 CustomException 유지)
    Book book = bookRepository.findById(request.bookId())
        .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));

    Users user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    // 중복 작성 검증 (1인 1리뷰)
    if (reviewRepository.existsByBookIdAndUserId(book.getId(), user.getId())) {
      throw new DuplicateReviewException();
    }

    // 리뷰 엔티티 생성 및 저장
    Review review = Review.builder()
        .bookId(book.getId())
        .userId(user.getId())
        .content(request.content())
        .rating(request.rating())
        .build();
    reviewRepository.save(review);

    // 도서 통계 업데이트 로직 호출
    updateBookStats(book, request.rating());
  }

  /**
   * 2. 리뷰 수정 (Update) - 리뷰 내용 및 평점 변경 (JPA 더티 체킹 활용)
   */
  @Override
  @Transactional
  public void updateReview(UUID reviewId, ReviewUpdateRequest request, UUID userId) {

    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> ReviewNotFoundException.withId(reviewId)); // ID 추적 기능(withId) 적용

    // 권한 확인: 수정을 요청한 사람이 실제 작성자인지 검사 (CustomException 추가)
    if (!review.getUserId().equals(userId)) {
      throw new ReviewForbiddenException();
    }

    // 내용 및 평점 업데이트
    review.updateReview(request.content(), request.rating());

  }

  /**
   * 3. 리뷰 삭제 (Delete - 논리 삭제) - 작성자 본인인지 권한 확인 - deleted_at 필드에 현재 시간을 찍어 논리적 삭제 처리
   */
  @Override
  @Transactional
  public void deleteReview(UUID reviewId, UUID userId) {

    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> ReviewNotFoundException.withId(reviewId));

    // 권한 확인
    if (!review.getUserId().equals(userId)) {
      throw new ReviewForbiddenException();
    }

    // 이미 삭제된 리뷰인지 확인
    if (review.getDeletedAt() != null) {
      throw ReviewAlreadyDeletedException.withId(reviewId);
    }

    // 물리 삭제 대신 BaseFullAuditEntity에서 제공하는 메서드로 논리 삭제 처리
    review.markAsDeleted();
  }

  /**
   * 4. 리뷰 목록 조회 (무한 스크롤 및 검색)
   */
  @Override
  public CursorPageResponseDto<ReviewDto> getReviews(ReviewListRequest request,
      UUID currentUserId) {

    Slice<Review> reviews = reviewRepository.searchReviews(request);
    Long totalElements = 0L; // 무한 스크롤이라 전체 개수는 임시로 0 처리
    
    return cursorPageResponseMapper.fromSlice(
        reviews,
        review -> {
          Book book = bookRepository.findById(review.getBookId()).orElse(null);
          Users user = userRepository.findById(review.getUserId()).orElse(null);

          boolean likedByMe = false;
          if (currentUserId != null) {
            likedByMe = reviewLikeRepository.existsByReviewIdAndUserId(review.getId(),
                currentUserId);
          }

          return reviewMapper.toDto(review, book, user, likedByMe);
        },
        Review::getId,
        Review::getCreatedAt,
        totalElements
    );
  }

  /**
   * [내부 로직] 도서 평균 평점 및 리뷰 수 계산 - Book 엔티티를 받아 공식을 적용하여 통계를 갱신합니다.
   */
  private void updateBookStats(Book book, Integer newRatingValue) {
    int newReviewCount = book.getReviewCount() + 1;

    // 기존 총점 = 기존 평균 평점 * 기존 리뷰 개수
    double currentTotal = book.getRating().doubleValue() * book.getReviewCount();

    // 새로운 평균 = (기존 총점 + 새로운 평점) / 새로운 리뷰 개수
    double newAverage = (currentTotal + newRatingValue) / newReviewCount;

    // 소수점 셋째 자리에서 반올림하여 둘째 자리까지 표현
    BigDecimal finalRating = BigDecimal.valueOf(newAverage).setScale(2, RoundingMode.HALF_UP);

    // Book 엔티티 업데이트
    book.updateRatingAndReviewCount(finalRating, newReviewCount);
  }
}