package com.sb09.deokhugam.domain.dashboard.repository;

import com.sb09.deokhugam.domain.dashboard.entity.PeriodType;
import com.sb09.deokhugam.domain.dashboard.entity.PopularBook;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PopularBookRepository extends JpaRepository<PopularBook, UUID> {

  //특정 period의 가장 최신 base_date조회 (최신 배치 결과 기준)
  Optional<PopularBook> findTopByPeriodOrderByBaseDateDesc(PeriodType period);
}
