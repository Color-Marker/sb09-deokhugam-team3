package com.sb09.deokhugam.global.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.hibernate.annotations.SQLRestriction;

@Getter
@MappedSuperclass
@SQLRestriction("deleted_at IS NULL")
public abstract class BaseDeleteableEntity extends BaseEntity {

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

//논리삭제

  //deletedAt 필드에 현재 시간을 기록하고 DB에서 실제로 데이터를 삭제하지 않고, "이 데이터는 몇 시 몇 분에 삭제된 것으로 간주한다"라고 마킹함
  public void markAsDeleted() {
    this.deletedAt = LocalDateTime.now();
  }

  //deletedAt 필드를 다시 null로 만들어서 삭제되었던 데이터를 다시 정상 상태로 복구할 때 사용함
  public void markAsRestored() {
    this.deletedAt = null;
  }
}
