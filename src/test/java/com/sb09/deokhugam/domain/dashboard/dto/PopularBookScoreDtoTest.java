package com.sb09.deokhugam.domain.dashboard.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PopularBookScoreDtoTest {

  @Test
  @DisplayName("PopularBookScoreDto가 생성될 때 null을 0으로 처리하고 점수(score)를 정확히 계산한다")
  void createPopularBookScoreDto_CalculateScore() {
    // given
    UUID bookId = UUID.randomUUID();
    Long reviewCount = 100L;
    Double avgRating = 4.5;

    // expected score: (100 * 0.4) + (4.5 * 0.6) = 40.0 + 2.7 = 42.7

    // when
    PopularBookScoreDto dto = new PopularBookScoreDto(bookId, reviewCount, avgRating);

    // then
    assertThat(dto.getBookId()).isEqualTo(bookId);
    assertThat(dto.getReviewCount()).isEqualTo(reviewCount);
    assertThat(dto.getAvgRating()).isEqualTo(avgRating);
    assertThat(dto.getScore()).isEqualTo(42.7);
  }

  @Test
  @DisplayName("PopularBookScoreDto에 Null 값이 들어와도 0으로 치환하여 에러 없이 계산된다")
  void createPopularBookScoreDto_WithNullValues() {
    // given
    UUID bookId = UUID.randomUUID();

    // when
    PopularBookScoreDto dto = new PopularBookScoreDto(bookId, null, null);

    // then
    assertThat(dto.getReviewCount()).isEqualTo(0L);
    assertThat(dto.getAvgRating()).isEqualTo(0.0);
    assertThat(dto.getScore()).isEqualTo(0.0);
  }
}