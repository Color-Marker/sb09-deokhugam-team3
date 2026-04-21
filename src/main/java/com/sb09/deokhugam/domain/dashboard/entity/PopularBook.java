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
    name = "popular_books",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_popular_books_target",
            columnNames = {"book_id", "period", "base_date"}
        )
    },
    indexes = {
        @Index(name = "idx_popular_books_period_rank", columnList = "period, base_date, ranking"),
        @Index(name = "idx_popular_books_created_at", columnList = "created_at")
    }
)
public class PopularBook {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "book_id", nullable = false)
  private UUID bookId;

  @Enumerated(EnumType.STRING)
  @Column(name = "period", nullable = false, length = 10)
  private PeriodType period; // DAILY, WEEKLY, MONTHLY, ALL_TIME

  @Column(name = "base_date", nullable = false)
  private LocalDate baseDate;

  @Column(name = "ranking", nullable = false)
  private Long ranking;

  @Column(name = "score", nullable = false, precision = 10, scale = 4)
  private BigDecimal score;

  @Column(name = "review_count", nullable = false)
  private Long reviewCount;

  @Column(name = "rating", nullable = false, precision = 3, scale = 2)
  private BigDecimal rating;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Builder
  public PopularBook(UUID bookId, PeriodType period, LocalDate baseDate, Long ranking, BigDecimal score, Long reviewCount, BigDecimal rating) {
    this.bookId = bookId;
    this.period = period;
    this.baseDate = baseDate;
    this.ranking = ranking;
    this.score = score;
    this.reviewCount = reviewCount;
    this.rating = rating;
  }
}