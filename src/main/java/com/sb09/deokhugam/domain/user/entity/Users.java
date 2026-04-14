package com.sb09.deokhugam.domain.user.entity;


import com.sb09.deokhugam.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Users extends BaseEntity {

  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @Column(nullable = false, length = 20)
  private String nickname;

  @Column(nullable = false, length = 255)
  private String password;

  // ==========================================
  // [추가된 부분] 파워 유저 랭킹 집계를 위한 반정규화(캐싱) 필드
  // ==========================================
  @Column(name = "review_score_sum", nullable = false)
  private Double reviewScoreSum = 0.0; // null 방지를 위해 기본값 0.0 설정

  @Column(name = "like_count", nullable = false)
  private Long likeCount = 0L; // null 방지를 위해 기본값 0L 설정

  @Column(name = "comment_count", nullable = false)
  private Long commentCount = 0L;

  // ==========================================

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Builder
  public Users(String email, String nickname, String password) {
    this.email = email;
    this.nickname = nickname;
    this.password = password;
  }

  public void updateNickname(String nickname) {
    this.nickname = nickname;
  }

  public void markAsDeleted() {
    this.deletedAt = LocalDateTime.now();
  }

  // ==========================================
  // [추가된 부분] 활동 수치 업데이트를 위한 비즈니스 메서드
  // ==========================================

  // 댓글이 작성될 때 호출
  public void increaseCommentCount() {
    this.commentCount++;
  }

  // 댓글이 삭제될 때 호출
  public void decreaseCommentCount() {
    if (this.commentCount > 0) {
      this.commentCount--;
    }
  }

  // 좋아요를 받았을 때 호출
  public void increaseLikeCount() {
    this.likeCount++;
  }

  // 좋아요가 취소되었을 때 호출
  public void decreaseLikeCount() {
    if (this.likeCount > 0) {
      this.likeCount--;
    }
  }

  // 리뷰가 작성되거나 수정될 때 호출 (기존 점수를 빼고 새 점수를 더하는 식의 로직이 필요할 수 있음)
  public void addReviewScore(Double score) {
    this.reviewScoreSum += score;
  }
}
