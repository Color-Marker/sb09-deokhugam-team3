package com.sb09.deokhugam.domain.dashboard.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class PopularBookScoreDto {
  private UUID bookId;
  private Long reviewCount;
  private Double avgRating;
  private Double score;

  public PopularBookScoreDto(UUID bookId, Long reviewCount, Double avgRating) {
    this.bookId = bookId;
    this.reviewCount = (reviewCount == null) ? 0L : reviewCount;
    this.avgRating = (avgRating == null) ? 0.0 : avgRating;

    // 요구사항: (리뷰 수 * 0.4) + (평균 별점 * 0.6)
    this.score = (this.reviewCount * 0.4) + (this.avgRating * 0.6);
  }
}