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

  // ==========================================
  // 논리 삭제 (Soft Delete) 및 중복 검사 쿼리 테스트
  // ==========================================

  @Test
  @DisplayName("논리 삭제 쿼리 테스트: existsBy... 메서드는 삭제된(deletedAt != null) 리뷰를 무시해야 한다")
  void existsByBookIdAndUserIdAndDeletedAtIsNull() {
    // given: 유저와 도서 준비
    Users user = Users.builder()
        .email("test2@test.com")
        .nickname("테스트유저2")
        .password("12345678")
        .build();
    userRepository.save(user);

    UUID bookId = UUID.randomUUID();

    // 1. 정상 리뷰 작성
    Review activeReview = Review.builder()
        .bookId(bookId)
        .userId(user.getId())
        .content("정상 리뷰입니다.")
        .rating(5)
        .build();
    reviewRepository.save(activeReview);

    // 2. 이미 논리 삭제된 리뷰 작성 (deletedAt 세팅)
    Review deletedReview = Review.builder()
        .bookId(bookId)
        .userId(user.getId())
        .content("삭제된 리뷰입니다.")
        .rating(1)
        .build();
    deletedReview.markAsDeleted(); // 논리 삭제 처리
    reviewRepository.save(deletedReview);

    // when & then 1: 정상 리뷰가 있으므로 true 반환
    boolean exists = reviewRepository.existsByBookIdAndUserIdAndDeletedAtIsNull(bookId,
        user.getId());
    assertThat(exists).isTrue();

    // given 2: 정상 리뷰마저 삭제해 버림
    activeReview.markAsDeleted();
    reviewRepository.save(activeReview);

    // when & then 2: 둘 다 삭제되었으므로 false 반환 (중복 아님!)
    boolean existsAfterDelete = reviewRepository.existsByBookIdAndUserIdAndDeletedAtIsNull(bookId,
        user.getId());
    assertThat(existsAfterDelete).isFalse();
  }

  // ==========================================
  //  인기 리뷰 기간별 조회 (날짜 필터링 + 논리 삭제 제외) 테스트
  // ==========================================

  @Test
  @DisplayName("기간별 인기 리뷰 쿼리: 특정 날짜 이후에 작성되고, 논리 삭제되지 않은 리뷰만 조회한다")
  void findByCreatedAtGreaterThanEqualAndDeletedAtIsNull() {
    // given: 유저 세팅
    Users user = Users.builder()
        .email("test3@test.com")
        .nickname("테스트유저3")
        .password("12345678")
        .build();
    userRepository.save(user);
    UUID bookId = UUID.randomUUID();

    // 리뷰 1: 정상 리뷰
    Review activeReview = Review.builder()
        .bookId(bookId)
        .userId(user.getId())
        .content("어제 쓴 정상 리뷰")
        .rating(5)
        .build();
    reviewRepository.save(activeReview);

    // 리뷰 2: 논리 삭제된 리뷰
    Review deletedReview = Review.builder()
        .bookId(bookId)
        .userId(user.getId())
        .content("삭제된 리뷰")
        .rating(4)
        .build();
    deletedReview.markAsDeleted();
    reviewRepository.save(deletedReview);

    // 조회 조건 세팅
    java.time.LocalDateTime pastDate = java.time.LocalDateTime.now().minusYears(10);
    org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(
        0, 10);

    // when: 기간 + 삭제 안 된 조건으로 DB 조회
    org.springframework.data.domain.Page<Review> result =
        reviewRepository.findByCreatedAtGreaterThanEqualAndDeletedAtIsNull(pastDate, pageRequest);

    // then: 총 2개를 저장했지만, 1개(삭제된 것)는 필터링되고 1개만 나와야 함
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getContent()).isEqualTo("어제 쓴 정상 리뷰");
  }

  @Test
  @DisplayName("전체 인기 리뷰 쿼리: ALL 조건일 때 논리 삭제된 리뷰를 제외하고 모두 조회한다")
  void findByDeletedAtIsNull() {
    // given: 유저 세팅
    Users user = Users.builder()
        .email("test4@test.com")
        .nickname("테스트유저4")
        .password("12345678")
        .build();
    userRepository.save(user);
    UUID bookId = UUID.randomUUID();

    // 정상 리뷰 2개, 삭제된 리뷰 1개 저장
    reviewRepository.save(
        Review.builder().bookId(bookId).userId(user.getId()).content("정상1").rating(5).build());
    reviewRepository.save(
        Review.builder().bookId(bookId).userId(user.getId()).content("정상2").rating(4).build());

    Review deletedReview = Review.builder().bookId(bookId).userId(user.getId()).content("삭제")
        .rating(3).build();
    deletedReview.markAsDeleted();
    reviewRepository.save(deletedReview);

    org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(
        0, 10);

    // when: 삭제 안 된 모든 리뷰 조회
    org.springframework.data.domain.Page<Review> result = reviewRepository.findByDeletedAtIsNull(
        pageRequest);

    // then: 총 3개 중 정상 리뷰 2개만 조회되어야 함!
    assertThat(result.getContent()).hasSize(2);
  }

  // ==========================================
  //  QueryDSL 동적 쿼리 (searchReviews) 커버리지 테스트
  // ==========================================

  @Test
  @DisplayName("QueryDSL 테스트: 무한 스크롤 동적 쿼리가 정상적으로 데이터를 필터링하고 슬라이싱(Slice)한다")
  void searchReviews_QueryDslTest() {
    // given (준비): 유저 생성 및 저장
    Users user = Users.builder()
        .email("query@test.com")
        .nickname("쿼리유저")
        .password("12345678")
        .build();
    userRepository.save(user);
    UUID bookId = UUID.randomUUID();

    // given (준비): 리뷰 3개를 DB에 연달아 저장 (무한 스크롤 테스트용)
    for (int i = 1; i <= 3; i++) {
      Review review = Review.builder()
          .bookId(bookId)
          .userId(user.getId())
          .content("QueryDSL 테스트 내용 " + i)
          .rating(5)
          .build();
      reviewRepository.save(review);
    }

    // given (준비): 프론트엔드에서 넘어오는 요청 세팅 (limit 2개, 최신순 정렬)
    com.sb09.deokhugam.domain.review.dto.request.ReviewListRequest request =
        new com.sb09.deokhugam.domain.review.dto.request.ReviewListRequest(
            bookId, null, null, 2, null, null, "LATEST",
            org.springframework.data.domain.Sort.Direction.DESC
        );

    // when (실행): 우리가 직접 만든 QueryDSL 커스텀 메서드 호출
    org.springframework.data.domain.Slice<com.sb09.deokhugam.domain.review.dto.response.ReviewDto> result =
        reviewRepository.searchReviews(request, user.getId());

    // then (검증)
    // 총 3개를 저장했지만 limit를 2로 주었으므로 2개만 조회되어야 함
    assertThat(result.getContent()).hasSize(2);
    // 3개 중 2개만 가져왔으므로, 다음 페이지가 존재해야 함 (hasNext = true)
    assertThat(result.hasNext()).isTrue();
  }
}