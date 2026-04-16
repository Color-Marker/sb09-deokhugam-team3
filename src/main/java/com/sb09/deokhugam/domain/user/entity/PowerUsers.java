package com.sb09.deokhugam.domain.user.entity;


import com.sb09.deokhugam.global.common.entity.BaseFullAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "power_users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PowerUsers extends BaseFullAuditEntity {

  // User와의 관계 (한 명의 유저는 랭킹 집계 기간별로 여러 개의 파워유저 기록을 가질 수 있음)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private Users user;

  @Column(nullable = false, length = 10)
  private String period; // DAILY, WEEKLY, MONTHLY, ALL_TIME

  @Column(name = "ranking", nullable = false)
  private Long rank;

  @Column(nullable = false, precision = 10, scale = 4)
  private Double score;

  @Column(name = "review_score_sum", nullable = false, precision = 10, scale = 4)
  private Double reviewScoreSum = 0.0;

  @Column(name = "like_count", nullable = false)
  private Long likeCount = 0L;

  @Column(name = "comment_count", nullable = false)
  private Long commentCount = 0L;

  @Builder
  public PowerUsers(Users user, String period, Long rank, Double score,
      Double reviewScoreSum, Long likeCount, Long commentCount) {
    this.user = user;
    this.period = period;
    this.rank = rank;
    this.score = score;
    this.reviewScoreSum = reviewScoreSum != null ? reviewScoreSum : 0.0;
    this.likeCount = likeCount != null ? likeCount : 0L;
    this.commentCount = commentCount != null ? commentCount : 0L;
  }
}
