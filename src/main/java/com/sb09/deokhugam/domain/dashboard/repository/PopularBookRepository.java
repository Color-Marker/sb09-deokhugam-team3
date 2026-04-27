package com.sb09.deokhugam.domain.dashboard.repository;

import com.sb09.deokhugam.domain.dashboard.entity.PeriodType;
import com.sb09.deokhugam.domain.dashboard.entity.PopularBook;
import java.util.Optional;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopularBookRepository extends JpaRepository<PopularBook, UUID> {

  //특정 period의 가장 최신 base_date조회 (최신 배치 결과 기준)
  Optional<PopularBook> findTopByPeriodOrderByBaseDateDesc(PeriodType period);

  boolean existsByBaseDate(LocalDate baseDate);

  void deleteByPeriodAndBaseDate(PeriodType period, LocalDate baseDate);
}
