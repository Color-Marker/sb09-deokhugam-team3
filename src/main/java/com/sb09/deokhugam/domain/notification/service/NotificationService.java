package com.sb09.deokhugam.domain.notification.service;

import com.sb09.deokhugam.domain.notification.dto.request.NotificationListRequest;
import com.sb09.deokhugam.domain.notification.dto.request.NotificationUpdateRequest;
import com.sb09.deokhugam.domain.notification.dto.response.NotificationDto;
import com.sb09.deokhugam.domain.notification.entity.Notification;
import com.sb09.deokhugam.domain.notification.entity.NotificationType;
import com.sb09.deokhugam.domain.review.entity.Review;
import com.sb09.deokhugam.domain.user.entity.Users;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import java.util.UUID;

public interface NotificationService {
  void readAll(UUID userId);
  NotificationDto updateStatus(UUID notificationId, UUID userId, NotificationUpdateRequest request);
  CursorPageResponseDto<NotificationDto> list(NotificationListRequest request);
  Notification create(NotificationType type, Review review, Users sender);
}
