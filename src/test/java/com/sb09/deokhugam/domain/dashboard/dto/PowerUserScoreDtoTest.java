package com.sb09.deokhugam.domain.dashboard.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PowerUserScoreDtoTest {

  @Test
  @DisplayName("파워 유저 점수 계산이 공식에 맞게 정확히 수행된다 (리뷰점수*0.5 + 좋아요*0.2 + 댓글*0.3)")
  void calculateScoreCorrectly() {
    // given
    UUID userId = UUID.randomUUID();
    Double reviewScoreSum = 10.0;
    Long likeCount = 10L;
    Long commentCount = 10L;

    // when
    PowerUserScoreDto dto = new PowerUserScoreDto(userId, reviewScoreSum, likeCount, commentCount);

    // then
    // (10.0 * 0.5) + (10 * 0.2) + (10 * 0.3) = 5.0 + 2.0 + 3.0 = 10.0
    assertThat(dto.getTotalScore()).isEqualTo(10.0);
  }

  @Test
  @DisplayName("각 항목이 null로 들어와도 0으로 치환되어 점수가 계산된다")
  void handleNullValues() {
    // given
    UUID userId = UUID.randomUUID();

    // when
    PowerUserScoreDto dto = new PowerUserScoreDto(userId, null, null, null);

    // then
    assertThat(dto.getReviewScoreSum()).isEqualTo(0.0);
    assertThat(dto.getLikeCount()).isEqualTo(0L);
    assertThat(dto.getCommentCount()).isEqualTo(0L);
    assertThat(dto.getTotalScore()).isEqualTo(0.0);
  }
}