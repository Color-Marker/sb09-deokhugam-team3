package com.sb09.deokhugam.domain.dashboard.service.basic;

import com.sb09.deokhugam.domain.dashboard.dto.PowerUserScoreDto;
import com.sb09.deokhugam.domain.dashboard.entity.PeriodType;
import com.sb09.deokhugam.domain.dashboard.entity.PowerUser;
import com.sb09.deokhugam.domain.dashboard.repository.DashboardQueryRepository;
import com.sb09.deokhugam.domain.dashboard.repository.PowerUserRepository;
import com.sb09.deokhugam.domain.dashboard.service.PowerUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class BasicPowerUserService implements PowerUserService {

  private final DashboardQueryRepository dashboardQueryRepository;
  private final PowerUserRepository powerUserRepository;

  @Transactional
  public long calculatePowerUser(LocalDate baseDate) {
    long ranking = 1;
    for (PeriodType period : PeriodType.values()) {
      // 1. 기존 데이터 깔끔하게 삭제
      powerUserRepository.deleteByPeriodAndBaseDate(period, baseDate);

      // 2. 기간 계산
      LocalDateTime from = calculateFrom(period, baseDate);
      LocalDateTime to = baseDate.atStartOfDay();

      // 3. QueryDSL로 상위 100개 파워 유저 가져오기
      List<PowerUserScoreDto> dtos = dashboardQueryRepository.findTopPowerUsers(from, to, 100);

      List<PowerUser> entitiesToSave = new ArrayList<>();

      // 4. Entity 변환
      for (PowerUserScoreDto dto : dtos) {
        entitiesToSave.add(PowerUser.builder()
            .userId(dto.getUserId())
            .period(period)
            .baseDate(baseDate)
            .ranking(ranking++)
            .score(BigDecimal.valueOf(dto.getTotalScore()))
            .reviewScoreSum(BigDecimal.valueOf(dto.getReviewScoreSum()))
            .likeCount(dto.getLikeCount())
            .commentCount(dto.getCommentCount())
            .build());
      }

      // 5. DB 저장
      powerUserRepository.saveAll(entitiesToSave);
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