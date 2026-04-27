package com.sb09.deokhugam.domain.review.repository;

import com.sb09.deokhugam.domain.review.entity.Review;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, UUID>, ReviewRepositoryCustom {

  // 리뷰 중복 검사
  boolean existsByBookIdAndUserIdAndDeletedAtIsNull(UUID bookId, UUID userId);

  @Query("""
      SELECT r.bookId, COUNT(r), AVG(r.rating)
      FROM Review r
      WHERE r.createdAt >= :from AND r.createdAt < :to
      AND r.deletedAt IS NULL
      GROUP BY r.bookId
      ORDER BY (COUNT(r) * 0.4 + AVG(r.rating) * 0.6) DESC
      """)
  List<Object[]> calculateBookByPeriod(@Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to);

  // 1. 기간 필터링(일간/주간/월간) + 삭제되지 않은 리뷰 조회
  Page<Review> findByCreatedAtGreaterThanEqualAndDeletedAtIsNull(LocalDateTime createdAt,
      Pageable pageable);

  // 2. 전체 기간(ALL) + 삭제되지 않은 리뷰 조회 (기존 findAll 대체용)
  Page<Review> findByDeletedAtIsNull(Pageable pageable);
}