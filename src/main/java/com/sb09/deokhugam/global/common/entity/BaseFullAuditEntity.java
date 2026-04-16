package com.sb09.deokhugam.global.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;

// updatedAt + deletedAt 둘 다 필요한 엔티티가 상속받을 클래스
@Getter
@MappedSuperclass
public abstract class BaseFullAuditEntity extends BaseUpdateableEntity {

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
  
  public void markAsDeleted() {
    this.deletedAt = LocalDateTime.now();
  }

  public void markAsRestored() {
    this.deletedAt = null;
  }
}