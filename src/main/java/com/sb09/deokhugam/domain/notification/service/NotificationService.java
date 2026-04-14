package com.sb09.deokhugam.domain.notification.service;

import com.sb09.deokhugam.domain.notification.dto.request.NotificationListRequest;
import com.sb09.deokhugam.domain.notification.dto.request.NotificationUpdateRequest;
import com.sb09.deokhugam.domain.notification.dto.response.NotificationDto;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

public interface NotificationService {
  ResponseEntity<Void> readAll(UUID userId);
  NotificationDto updateStatus(UUID notificationId, UUID userId, NotificationUpdateRequest request);
  CursorPageResponseDto<NotificationDto> list(NotificationListRequest request);
}
