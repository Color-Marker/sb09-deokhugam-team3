package com.sb09.deokhugam.domain.user.entity;


import com.sb09.deokhugam.global.common.entity.BaseFullAuditEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class Users extends BaseFullAuditEntity {

  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @Column(nullable = false, length = 20)
  private String nickname;

  @Column(nullable = false, length = 255)
  private String password;

  // 파워 유저 랭킹 집계를 위한 필드
  @Column(name = "review_score_sum", nullable = false)
  private Double reviewScoreSum = 0.0;

  @Column(name = "like_count", nullable = false)
  private Long likeCount = 0L;

  @Column(name = "comment_count", nullable = false)
  private Long commentCount = 0L;

  @Builder
  public Users(String email, String nickname, String password) {
    this.email = email;
    this.nickname = nickname;
    this.password = password;
  }

  public void updateNickname(String nickname) {
    this.nickname = nickname;
  }

  public void increaseCommentCount() {
    this.commentCount++;
  }

  public void decreaseCommentCount() {
    if (this.commentCount > 0) {
      this.commentCount--;
    }
  }

  public void increaseLikeCount() {
    this.likeCount++;
  }

  public void decreaseLikeCount() {
    if (this.likeCount > 0) {
      this.likeCount--;
    }
  }

  public void addReviewScore(Double score) {
    this.reviewScoreSum += score;
  }
}
