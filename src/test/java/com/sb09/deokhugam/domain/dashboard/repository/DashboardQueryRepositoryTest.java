package com.sb09.deokhugam.domain.dashboard.repository;

import com.sb09.deokhugam.config.QueryDslConfig;
import com.sb09.deokhugam.domain.dashboard.dto.PopularReviewScoreDto;
import com.sb09.deokhugam.domain.review.entity.Review;
import com.sb09.deokhugam.domain.review.repository.ReviewRepository;
import com.sb09.deokhugam.domain.user.entity.Users;
import com.sb09.deokhugam.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
// 💡 아래에 만든 감시자 설정(JpaAuditingTestConfig)을 Import에 추가했습니다!
@Import({QueryDslConfig.class, DashboardQueryRepository.class, DashboardQueryRepositoryTest.JpaAuditingTestConfig.class})
class DashboardQueryRepositoryTest {

  // 💡 테스트 환경 전용 JPA Auditing(감시자) 활성화 설정!
  @TestConfiguration
  @EnableJpaAuditing
  static class JpaAuditingTestConfig {}

  @Autowired
  private DashboardQueryRepository dashboardQueryRepository;

  @Autowired
  private ReviewRepository reviewRepository;

  @Autowired
  private UserRepository userRepository;

  @Test
  @DisplayName("기간 내 활동이 없는 리뷰는 제외되고, 활동이 있는 리뷰만 점수 순으로 조회된다.")
  void findTopPopularReviews_FilterAndSort() {
    // given
    Users user = userRepository.save(Users.builder()
        .email("test@test.com").nickname("tester").password("1234").build());

    Review review1 = reviewRepository.save(Review.builder()
        .bookId(UUID.randomUUID()).userId(user.getId()).content("리뷰1").rating(5).build());

    LocalDateTime start = LocalDateTime.now().minusDays(2);
    LocalDateTime end = LocalDateTime.now().plusDays(1);

    // when
    List<PopularReviewScoreDto> results = dashboardQueryRepository.findTopPopularReviews(start, end, 10);

    // then
    assertThat(results).isEmpty();
  }
}