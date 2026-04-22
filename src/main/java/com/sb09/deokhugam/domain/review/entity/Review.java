package com.sb09.deokhugam.domain.review.entity;

import com.sb09.deokhugam.global.common.entity.BaseFullAuditEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reviews")
public class Review extends BaseFullAuditEntity {

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

  // Review가 삭제될 때, ReviewLike도 자동으로 모두 삭제됩니다.
  @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ReviewLike> likes = new ArrayList<>();

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