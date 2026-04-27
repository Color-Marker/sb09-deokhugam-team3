package com.sb09.deokhugam.domain.dashboard.repository;

import com.sb09.deokhugam.domain.dashboard.entity.PeriodType;
import com.sb09.deokhugam.domain.dashboard.entity.PopularReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface PopularReviewRepository extends JpaRepository<PopularReview, UUID> {

  // 특정 period의 가장 최신 base_date 조회
  Optional<PopularReview> findTopByPeriodOrderByBaseDateDesc(PeriodType period);

  // 해당 날짜의 통계가 이미 존재하는지 확인
  boolean existsByBaseDate(LocalDate baseDate);

  // 특정 기간/날짜의 기존 통계 삭제
  void deleteByPeriodAndBaseDate(PeriodType period, LocalDate baseDate);
}