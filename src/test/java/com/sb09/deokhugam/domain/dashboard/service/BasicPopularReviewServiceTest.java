package com.sb09.deokhugam.domain.dashboard.service;

import com.sb09.deokhugam.domain.dashboard.dto.PopularReviewScoreDto;
import com.sb09.deokhugam.domain.dashboard.entity.PeriodType;
import com.sb09.deokhugam.domain.dashboard.event.PopularReviewTop10Event;
import com.sb09.deokhugam.domain.dashboard.repository.DashboardQueryRepository;
import com.sb09.deokhugam.domain.dashboard.repository.PopularReviewRepository;
import com.sb09.deokhugam.domain.dashboard.service.basic.BasicPopularReviewService;
import com.sb09.deokhugam.domain.notification.entity.NotificationType;
import com.sb09.deokhugam.domain.notification.service.NotificationService;
import com.sb09.deokhugam.domain.review.entity.Review;
import com.sb09.deokhugam.domain.review.repository.ReviewRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasicPopularReviewServiceTest {

  @InjectMocks
  private BasicPopularReviewService popularReviewService;

  @Mock
  private DashboardQueryRepository dashboardQueryRepository;

  @Mock
  private PopularReviewRepository popularReviewRepository;

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private NotificationService notificationService;

  @Test
  @DisplayName("인기 리뷰 배치 로직이 정상적으로 수행되며, Top 10 진입 시 알림을 생성한다.")
  void calculatePopularReview_Success() {
    // given
    LocalDate baseDate = LocalDate.of(2026, 4, 27);
    List<PopularReviewScoreDto> mockDtos = new ArrayList<>();

    // 12개의 가짜 리뷰 데이터 생성 (1~10위는 알림 발생, 11~12위는 알림 미발생)
    for (int i = 1; i <= 12; i++) {
      mockDtos.add(new PopularReviewScoreDto(UUID.randomUUID(), 10L, 10L));
    }

    // 레포지토리가 가짜 데이터를 반환하도록 설정
    when(dashboardQueryRepository.findTopPopularReviews(any(), any(), eq(100)))
        .thenReturn(mockDtos);

    // Top 10 조회 시 가짜 Review 반환 (mock)
    Review mockReview = mock(Review.class);
    when(reviewRepository.findById(any(UUID.class)))
        .thenReturn(Optional.of(mockReview));

    // when
    popularReviewService.calculatePopularReview(baseDate);

    // then
    // 1. 4가지 기간(DAILY, WEEKLY, MONTHLY, ALL_TIME)에 대해 삭제 로직이 호출되었는가?
    verify(popularReviewRepository, times(4)).deleteByPeriodAndBaseDate(any(PeriodType.class), eq(baseDate));

    // 2. 4가지 기간에 대해 저장 로직이 호출되었는가?
    verify(popularReviewRepository, times(4)).saveAll(anyList());

    // 3. reviewRepository.findById()가 정확히 (10위 이내 * 4개 기간 = 40번) 호출되었는가?
    verify(reviewRepository, times(40)).findById(any(UUID.class));

    // 4. notificationService.create()가 정확히 40번 호출되었는가?
    verify(notificationService, times(40)).create(eq(NotificationType.RANKING), eq(mockReview), eq(null));
  }
}
