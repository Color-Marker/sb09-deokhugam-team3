package com.sb09.deokhugam.domain.comment.entity;

import com.sb09.deokhugam.global.common.entity.BaseFullAuditEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
//@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comments extends BaseFullAuditEntity {

  private UUID id;

  private UUID reviewId;

  private UUID userId;

  private String userNickname;

  private String content;

  public Comments(UUID id, UUID reviewId, UUID userId, String userNickname, String content) {
    this.id = id;
    this.reviewId = reviewId;
    this.userId = userId;
    this.userNickname = userNickname;
    this.content = content;
  }
}


