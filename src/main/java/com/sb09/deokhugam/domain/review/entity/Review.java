package com.sb09.deokhugam.domain.review.entity;

import com.sb09.deokhugam.global.common.entity.BaseUpdateableEntity;
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
    name = "reviews",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_book_user",
            columnNames = {"book_id", "user_id"}
        )
    }
)
public class Review extends BaseUpdateableEntity {

  @Column(name = "book_id", nullable = false)
  private UUID bookId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(nullable = false)
  private Integer rating;

  @Column(name = "like_count", nullable = false)
  private Integer likeCount = 0;

  @Column(name = "comment_count", nullable = false)
  private Integer commentCount = 0;

  // 논리 삭제를 위한 필드 추가 (null이면 정상, 시간이 있으면 삭제됨)
  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Builder
  public Review(UUID bookId, UUID userId, String content, Integer rating) {
    this.bookId = bookId;
    this.userId = userId;
    this.content = content;
    this.rating = rating;
  }

  
  public void updateReview(String content, Integer rating) {
    this.content = content;
    this.rating = rating;
  }

  public void deleteSoftly() {
    this.deletedAt = LocalDateTime.now();
  }

  public void addLikeCount() {
    this.likeCount++;
  }

  public void removeLikeCount() {
    if (this.likeCount > 0) {
      this.likeCount--;
    }
  }

  public void addCommentCount() {
    this.commentCount++;
  }

  public void removeCommentCount() {
    if (this.commentCount > 0) {
      this.commentCount--;
    }
  }
}