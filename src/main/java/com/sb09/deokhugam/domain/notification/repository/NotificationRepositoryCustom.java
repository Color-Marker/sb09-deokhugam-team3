package com.sb09.deokhugam.domain.notification.repository;

import com.sb09.deokhugam.domain.notification.dto.request.NotificationListRequest;
import com.sb09.deokhugam.domain.notification.entity.Notification;
import java.time.LocalDateTime;
import org.springframework.data.domain.Slice;

public interface NotificationRepositoryCustom {
  Slice<Notification> searchNotification(NotificationListRequest request);
  Long countNotification(NotificationListRequest request);
  long deleteOldNotification(LocalDateTime duration);
}
