package com.sb09.deokhugam.domain.review.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "popular_reviews",
    // SQL의 UNIQUE 제약조건을 엔티티에도 명시하여 무결성 보장
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_popular_reviews_review_period_date",
            columnNames = {"review_id", "period", "base_date"}
        )
    }
)
public class PopularReview {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "review_id", nullable = false)
  private UUID reviewId;

  @Column(nullable = false, length = 10)
  private String period; // DAILY, WEEKLY, MONTHLY, ALL_TIME

  @Column(name = "ranking", nullable = false)
  private Long ranking;

  @Column(name = "base_date", nullable = false)
  private LocalDate baseDate; // 새로 추가 기준일 (DATE 타입은 LocalDate 사용)

  @Column(nullable = false, precision = 10, scale = 4)
  private BigDecimal score;

  @Column(name = "like_count", nullable = false)
  private Long likeCount = 0L;

  @Column(name = "comment_count", nullable = false)
  private Long commentCount = 0L;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Builder
  public PopularReview(UUID reviewId, String period, Long ranking, LocalDate baseDate,
      BigDecimal score, Long likeCount, Long commentCount) {
    this.reviewId = reviewId;
    this.period = period;
    this.ranking = ranking;
    this.baseDate = baseDate;
    this.score = score;
    this.likeCount = likeCount != null ? likeCount : 0L;
    this.commentCount = commentCount != null ? commentCount : 0L;
  }
}