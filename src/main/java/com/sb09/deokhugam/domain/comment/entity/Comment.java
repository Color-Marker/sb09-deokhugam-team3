package com.sb09.deokhugam.domain.comment.entity;

import com.sb09.deokhugam.domain.review.entity.Review;
import com.sb09.deokhugam.domain.user.entity.Users;
import com.sb09.deokhugam.global.common.entity.BaseFullAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseFullAuditEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "review_id", nullable = false)
  private Review reviewId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private Users userId;

  @Column(name = "content", nullable = false)
  private String content;

  public Comment(Review reviewId, Users userId, String content) {
    this.reviewId = reviewId;
    this.userId = userId;
    this.content = content;
  }

  public void updateContent(String content) {
    this.content = content;
  }
}

