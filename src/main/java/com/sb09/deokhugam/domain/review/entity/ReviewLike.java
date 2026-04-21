package com.sb09.deokhugam.domain.review.entity;

import com.sb09.deokhugam.domain.user.entity.Users;
import com.sb09.deokhugam.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "review_likes",
    uniqueConstraints = {
        // 유저 한 명은 한 리뷰에 좋아요를 한 번만 누를 수 있음
        @UniqueConstraint(
            name = "uq_review_likes_review_user",
            columnNames = {"review_id", "user_id"}
        )
    }
)
public class ReviewLike extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "review_id", nullable = false)
  private Review review;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private Users user;

  @Builder
  public ReviewLike(Review review, Users user) {
    this.review = review;
    this.user = user;
  }
}