package com.sb09.deokhugam.domain.dashboard.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class PowerUserScoreDto {
  private UUID userId;
  private Double reviewScoreSum; // 해당 유저가 쓴 리뷰들의 인기 점수 합
  private Long likeCount;  // 유저가 누른 좋아요 수
  private Long commentCount; // 유저가 쓴 댓글 수
  private Double totalScore;

  public PowerUserScoreDto(UUID userId, Double reviewScoreSum, Long activeLikeCount, Long activeCommentCount) {
    this.userId = userId;
    this.reviewScoreSum = (reviewScoreSum == null) ? 0.0 : reviewScoreSum;
    this.likeCount = (activeLikeCount == null) ? 0L : activeLikeCount;
    this.commentCount = (activeCommentCount == null) ? 0L : activeCommentCount;

    // 요구사항: (리뷰점수합 * 0.5) + (참여 좋아요 * 0.2) + (참여 댓글 * 0.3)
    this.totalScore = (this.reviewScoreSum * 0.5) + (this.likeCount * 0.2) + (this.commentCount * 0.3);
  }
}