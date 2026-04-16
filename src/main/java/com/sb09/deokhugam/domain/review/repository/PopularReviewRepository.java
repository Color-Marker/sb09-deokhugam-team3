package com.sb09.deokhugam.domain.review.repository;

import com.sb09.deokhugam.domain.review.entity.PopularReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PopularReviewRepository extends JpaRepository<PopularReview, UUID> {

  // 기간별(DAILY, WEEKLY 등)로 랭킹 오름차순(1등부터)으로 인기 리뷰 목록 가져오기
  List<PopularReview> findAllByPeriodOrderByRankAsc(String period);
}