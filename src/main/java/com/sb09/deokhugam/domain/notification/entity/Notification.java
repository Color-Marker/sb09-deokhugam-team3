package com.sb09.deokhugam.domain.notification.entity;

import com.sb09.deokhugam.domain.review.entity.Review;
import com.sb09.deokhugam.global.common.entity.BaseUpdateableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.catalina.User;

@Entity
@Table(name = "notifications")
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseUpdateableEntity {

  @Column(name = "confirm_status", nullable = false)
  private boolean confirmStatus;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private NotificationType type;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "review_id", columnDefinition = "uuid")
  private Review review;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_id", columnDefinition = "uuid")
  private User sender;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", columnDefinition = "uuid", nullable = false)
  private User user;

}
