package com.sb09.deokhugam.domain.dashboard.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class PopularReviewScoreDto {
  private UUID reviewId;
  private Long likeCount;
  private Long commentCount;
  private Double score;

  public PopularReviewScoreDto(UUID reviewId, Long likeCount, Long commentCount) {
    this.reviewId = reviewId;
    this.likeCount = (likeCount == null) ? 0L : likeCount;
    this.commentCount = (commentCount == null) ? 0L : commentCount;
    // 요구사항: (좋아요 * 0.3) + (댓글 * 0.7)
    this.score = (this.likeCount * 0.3) + (this.commentCount * 0.7);
  }
}