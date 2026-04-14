package com.sb09.deokhugam.domain.notification.service.basic;

import com.sb09.deokhugam.domain.notification.mapper.NotificationMapper;
import com.sb09.deokhugam.domain.notification.repository.NotificationRepository;
import com.sb09.deokhugam.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicNotificationService implements NotificationService {
  private final NotificationRepository notificationRepository;
  private final NotificationMapper notificationMapper;
}
