package com.sb09.deokhugam.domain.review.service;

import com.sb09.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sb09.deokhugam.domain.review.entity.Review;
import com.sb09.deokhugam.domain.review.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

  private final ReviewRepository reviewRepository;
  
  @Transactional
  public void createReview(ReviewCreateRequest request, UUID userId) {
    if (reviewRepository.existsByBookIdAndUserId(request.bookId(), userId)) {
      throw new IllegalArgumentException("이미 이 도서에 작성한 리뷰가 존재합니다.");
    }

    Review review = Review.builder()
        .bookId(request.bookId())
        .userId(userId)
        .content(request.content())
        .rating(request.rating())
        .build();

    reviewRepository.save(review);
  }
}
