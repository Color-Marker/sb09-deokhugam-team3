package com.sb09.deokhugam.domain.review.repository;

import com.sb09.deokhugam.domain.review.entity.PopularReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PopularReviewRepository extends JpaRepository<PopularReview, UUID> {

  // 특정 날짜(baseDate)와 특정 기간(period)의 인기 리뷰 목록을 랭킹(ranking) 오름차순으로 조회
  List<PopularReview> findAllByPeriodAndBaseDateOrderByRankingAsc(String period,
      LocalDate baseDate);

}