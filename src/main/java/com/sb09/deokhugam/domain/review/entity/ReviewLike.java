package com.sb09.deokhugam.domain.review.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
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
public class ReviewLike {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "review_id", nullable = false)
  private UUID reviewId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Builder
  public ReviewLike(UUID reviewId, UUID userId) {
    this.reviewId = reviewId;
    this.userId = userId;
  }
}