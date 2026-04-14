package com.sb09.deokhugam.domain.notification.service.basic;

import com.sb09.deokhugam.domain.notification.dto.request.NotificationListRequest;
import com.sb09.deokhugam.domain.notification.dto.request.NotificationUpdateRequest;
import com.sb09.deokhugam.domain.notification.dto.response.NotificationDto;
import com.sb09.deokhugam.domain.notification.entity.Notification;
import com.sb09.deokhugam.domain.notification.mapper.NotificationMapper;
import com.sb09.deokhugam.domain.notification.repository.NotificationRepository;
import com.sb09.deokhugam.domain.notification.service.NotificationService;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BasicNotificationService implements NotificationService {
  private final NotificationRepository notificationRepository;
  private final NotificationMapper notificationMapper;

  @Override
  public ResponseEntity<Void> readAll(UUID userId) {
    if(userId == null){
      log.warn("사용자 ID가 입력되지 않았습니다.");

    }
    List<Notification> notis = notificationRepository.findAllByUserId(userId);
    for (Notification n : notis){
      n.update();
    }
    return null;
  }

  @Override
  public NotificationDto updateStatus(UUID notificationId, UUID userId,
      NotificationUpdateRequest request) {

    return null;
  }

  @Override
  public CursorPageResponseDto<NotificationDto> list(NotificationListRequest request) {
    return null;
  }
}
