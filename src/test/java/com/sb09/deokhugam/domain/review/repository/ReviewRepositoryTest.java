package com.sb09.deokhugam.domain.review.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sb09.deokhugam.config.QueryDslConfig;
import com.sb09.deokhugam.domain.review.entity.Review;
import com.sb09.deokhugam.domain.review.entity.ReviewLike;
import com.sb09.deokhugam.domain.user.entity.Users;
import com.sb09.deokhugam.domain.user.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@DataJpaTest
@Import(QueryDslConfig.class)
class ReviewRepositoryTest {

  @TestConfiguration
  @EnableJpaAuditing
  static class JpaAuditingTestConfig {

  }

  @Autowired
  private ReviewRepository reviewRepository;

  @Autowired
  private ReviewLikeRepository reviewLikeRepository;

  @Autowired
  private UserRepository userRepository;

  @Test
  @DisplayName("물리 삭제 테스트: 리뷰 삭제 시 연관된 좋아요(ReviewLike)도 함께 Cascade 삭제된다")
  void deleteReview_CascadeReviewLike() {
    // given (준비 단계)

    // 1. 가짜 유저 생성 후 DB 저장
    // 주의: Users 엔티티에 필수값(이메일, 닉네임, 비밀번호 등)이 있다면 아래 builder() 안에 꼭 채워주세요!
    Users user = Users.builder()
        .email("test@test.com")
        .nickname("테스트유저")
        .password("12345678")
        .build();
    userRepository.save(user);

    // 2. 가짜 리뷰 생성 후 DB 저장
    UUID bookId = UUID.randomUUID();
    Review review = Review.builder()
        .bookId(bookId)
        .userId(user.getId())
        .content("정말 감명 깊게 읽었습니다.")
        .rating(5)
        .build();
    reviewRepository.save(review);

    // 3. 리뷰에 좋아요 추가 후 DB 저장
    ReviewLike reviewLike = ReviewLike.builder()
        .review(review)
        .user(user)
        .build();
    reviewLikeRepository.save(reviewLike);

    // 4. 양방향 연관관계 세팅 (리뷰 객체의 좋아요 리스트에 추가)
    review.getLikes().add(reviewLike);

    // when (실행 단계)

    // 5. 리뷰를 '물리 삭제' 합니다. (Service의 논리 삭제가 아닌 실제 DB 삭제 명령어)
    reviewRepository.delete(review);
    reviewRepository.flush(); // DB에 즉시 쿼리를 날리도록 강제 동기화

    // then (검증 단계)

    // 6. 좋아요가 진짜로 같이 지워졌는지 확인 (DB에 남은 좋아요 개수가 0개여야 성공)
    long likeCount = reviewLikeRepository.count();
    assertThat(likeCount).isEqualTo(0L);
  }
}