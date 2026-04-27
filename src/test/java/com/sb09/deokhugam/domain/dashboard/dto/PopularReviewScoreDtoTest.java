package com.sb09.deokhugam.domain.dashboard.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PopularReviewScoreDtoTest {

  @Test
  @DisplayName("인기 리뷰 점수 계산이 공식에 맞게 정확히 수행된다 (좋아요 * 0.3 + 댓글 * 0.7)")
  void calculateScoreCorrectly() {
    // given
    UUID reviewId = UUID.randomUUID();
    Long likeCount = 10L;
    Long commentCount = 10L;

    // when
    PopularReviewScoreDto dto = new PopularReviewScoreDto(reviewId, likeCount, commentCount);

    // then
    // 10 * 0.3 + 10 * 0.7 = 3.0 + 7.0 = 10.0
    assertThat(dto.getScore()).isEqualTo(10.0);
    assertThat(dto.getLikeCount()).isEqualTo(10L);
    assertThat(dto.getCommentCount()).isEqualTo(10L);
  }

  @Test
  @DisplayName("좋아요나 댓글 수가 null로 들어와도 0으로 치환되어 점수가 계산된다")
  void handleNullValues() {
    // given
    UUID reviewId = UUID.randomUUID();

    // when
    PopularReviewScoreDto dto = new PopularReviewScoreDto(reviewId, null, null);

    // then
    assertThat(dto.getLikeCount()).isEqualTo(0L);
    assertThat(dto.getCommentCount()).isEqualTo(0L);
    assertThat(dto.getScore()).isEqualTo(0.0);
  }
}