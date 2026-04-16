package com.sb09.deokhugam.domain.dashboard.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "popular_reviews",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_popular_reviews_target",
            columnNames = {"review_id", "period", "base_date"}
        )
    },
    indexes = {
        @Index(name = "idx_popular_reviews_period_rank", columnList = "period, base_date, rank"),
        @Index(name = "idx_popular_reviews_created_at", columnList = "created_at")
    }
)
public class PopularReview {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "review_id", nullable = false)
  private UUID reviewId;

  @Enumerated(EnumType.STRING)
  @Column(name = "period", nullable = false, length = 10)
  private PeriodType period;

  @Column(name = "base_date", nullable = false)
  private LocalDate baseDate;

  @Column(name = "ranking", nullable = false)
  private Long ranking;

  @Column(name = "score", nullable = false, precision = 10, scale = 4)
  private BigDecimal score;

  @Column(name = "like_count", nullable = false)
  private Long likeCount;

  @Column(name = "comment_count", nullable = false)
  private Long commentCount;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Builder
  public PopularReview(UUID reviewId, PeriodType period, LocalDate baseDate, Long ranking, BigDecimal score, Long likeCount, Long commentCount) {
    this.reviewId = reviewId;
    this.period = period;
    this.baseDate = baseDate;
    this.ranking = ranking;
    this.score = score;
    this.likeCount = likeCount;
    this.commentCount = commentCount;
  }
}