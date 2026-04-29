package com.sb09.deokhugam.domain.dashboard.service.basic;

import com.sb09.deokhugam.domain.dashboard.dto.PopularReviewScoreDto;
import com.sb09.deokhugam.domain.dashboard.entity.PeriodType;
import com.sb09.deokhugam.domain.dashboard.entity.PopularReview;
import com.sb09.deokhugam.domain.dashboard.event.PopularReviewTop10Event;
import com.sb09.deokhugam.domain.dashboard.repository.DashboardQueryRepository;
import com.sb09.deokhugam.domain.dashboard.repository.PopularReviewRepository;
import com.sb09.deokhugam.domain.dashboard.service.PopularReviewService;
import com.sb09.deokhugam.domain.notification.entity.NotificationType;
import com.sb09.deokhugam.domain.notification.service.NotificationService;
import com.sb09.deokhugam.domain.review.entity.Review;
import com.sb09.deokhugam.domain.review.repository.ReviewRepository;
import com.sb09.deokhugam.global.exception.review.ReviewNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicPopularReviewService implements PopularReviewService {

  private final DashboardQueryRepository dashboardQueryRepository;
  private final PopularReviewRepository popularReviewRepository;
  private final ReviewRepository reviewRepository;
  private final NotificationService notificationService;

  @Transactional
  public long calculatePopularReview(LocalDate baseDate) {
    long ranking = 1;
    for (PeriodType period : PeriodType.values()) {
      // 1. 기존 데이터 깔끔하게 삭제 (1안 적용!)
      popularReviewRepository.deleteByPeriodAndBaseDate(period, baseDate);

      // 2. 기간 계산 (팀원의 calculateFrom 패턴과 동일)
      LocalDateTime from = calculateFrom(period, baseDate);
      LocalDateTime to = baseDate.atStartOfDay();

      // 3. QueryDSL로 점수가 계산된 상위 100개 데이터 가져오기
      List<PopularReviewScoreDto> dtos = dashboardQueryRepository.findTopPopularReviews(from, to, 100);

      List<PopularReview> entitiesToSave = new ArrayList<>();

      // 4. Entity 변환 및 알림 이벤트 발행
      for (PopularReviewScoreDto dto : dtos) {

        // Top 10 이내면 이벤트 발행
        if (ranking <= 10) {
          Review review = reviewRepository.findById(dto.getReviewId()).orElseThrow(() ->
              ReviewNotFoundException.withId(dto.getReviewId()));
          notificationService.create(NotificationType.RANKING, review, null);
        }

        entitiesToSave.add(PopularReview.builder()
            .reviewId(dto.getReviewId())
            .period(period)
            .baseDate(baseDate)
            .ranking(ranking++)
            .score(BigDecimal.valueOf(dto.getScore()))
            .likeCount(dto.getLikeCount())
            .commentCount(dto.getCommentCount())
            .build());
      }

      // 5. 한 번에 DB 저장
      popularReviewRepository.saveAll(entitiesToSave);
    }
    return (ranking-1);
  }

  private LocalDateTime calculateFrom(PeriodType period, LocalDate baseDate) {
    return switch (period) {
      case DAILY -> baseDate.minusDays(1).atStartOfDay();
      case WEEKLY -> baseDate.minusWeeks(1).atStartOfDay();
      case MONTHLY -> baseDate.minusMonths(1).atStartOfDay();
      case ALL_TIME -> LocalDateTime.of(2000, 1, 1, 0, 0);
    };
  }
}