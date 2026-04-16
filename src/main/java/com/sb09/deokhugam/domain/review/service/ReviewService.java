package com.sb09.deokhugam.domain.review.service;

import com.sb09.deokhugam.domain.book.entity.Book;
import com.sb09.deokhugam.domain.book.repository.BookRepository;
import com.sb09.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sb09.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.sb09.deokhugam.domain.review.entity.Review;
import com.sb09.deokhugam.domain.review.repository.ReviewRepository;
import com.sb09.deokhugam.domain.user.entity.Users;
import com.sb09.deokhugam.domain.user.repository.UserRepository;

import com.sb09.deokhugam.global.Exception.CustomException;
import com.sb09.deokhugam.global.Exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final BookRepository bookRepository;
  private final UserRepository userRepository;

  /**
   * 1. 리뷰 등록 (Create)
   */
  @Transactional
  public void createReview(ReviewCreateRequest request, UUID userId) {

    // 도서와 유저가 실제로 DB에 존재하는지 확인
    Book book = bookRepository.findById(request.bookId())
        .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));

    Users user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    // 중복 작성 검증 (1인 1리뷰)
    if (reviewRepository.existsByBookIdAndUserId(book.getId(), user.getId())) {
      throw new CustomException(ErrorCode.DUPLICATE_REVIEW);
    }

    // 리뷰 엔티티 생성 및 저장
    Review review = Review.builder()
        .bookId(book.getId())
        .userId(user.getId())
        .content(request.content())
        .rating(request.rating())
        .build();
    reviewRepository.save(review);

    // 유저 활동 점수 업데이트 (Users 엔티티의 메서드 사용)
    // user.addReviewScore(request.rating().doubleValue());

    // 도서 통계 업데이트 로직 호출
    updateBookStats(book, request.rating());
  }

  /**
   * 2. 리뷰 수정 (Update) - 리뷰 내용 및 평점 변경 (JPA 더티 체킹 활용)
   */
  @Transactional
  public void updateReview(UUID reviewId, ReviewUpdateRequest request, UUID userId) {

    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

    // 권한 확인: 수정을 요청한 사람이 실제 작성자인지 검사 (CustomException 추가)
    if (!review.getUserId().equals(userId)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
    }

    // 내용 및 평점 업데이트
    review.updateReview(request.content(), request.rating());

    // (선택 심화) 평점이 바뀌었다면 도서의 평균 평점과 유저의 총점도 다시 계산해야 하지만,
    // 로직이 복잡해지므로 일단 내용과 평점 수정만 적용합니다.
  }

  /**
   * 3. 리뷰 삭제 (Delete - 논리 삭제) - 작성자 본인인지 권한 확인 - deleted_at 필드에 현재 시간을 찍어 논리적 삭제 처리
   */
  @Transactional
  public void deleteReview(UUID reviewId, UUID userId) {

    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

    // 권한 확인
    if (!review.getUserId().equals(userId)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
    }

    // 이미 삭제된 리뷰인지 확인
    if (review.getDeletedAt() != null) {
      throw new CustomException(ErrorCode.DELETED_REVIEW);
    }

    // 물리 삭제(reviewRepository.delete(review)) 대신 논리 삭제 처리
    review.deleteSoftly();
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