package com.sb09.deokhugam.domain.notification.service.basic;

import com.sb09.deokhugam.domain.notification.dto.request.NotificationListRequest;
import com.sb09.deokhugam.domain.notification.dto.request.NotificationUpdateRequest;
import com.sb09.deokhugam.domain.notification.dto.response.NotificationDto;
import com.sb09.deokhugam.domain.notification.entity.Notification;
import com.sb09.deokhugam.domain.notification.entity.NotificationType;
import com.sb09.deokhugam.domain.notification.mapper.NotificationMapper;
import com.sb09.deokhugam.domain.notification.repository.NotificationRepository;
import com.sb09.deokhugam.domain.notification.service.NotificationService;
import com.sb09.deokhugam.domain.user.entity.Users;
import com.sb09.deokhugam.domain.user.repository.UserRepository;
import com.sb09.deokhugam.global.Exception.CustomException;
import com.sb09.deokhugam.global.Exception.ErrorCode;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import java.util.List;
import java.util.Optional;
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
  private final UserRepository userRepository;
  private final NotificationMapper notificationMapper;

  @Override
  public void readAll(UUID userId) {
    if(userId == null){
      log.warn("잘못된 요청입니다.");
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }
    if(!userRepository.existsById(userId)){
      log.warn("사용자를 찾을 수 없습니다.");
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }
    List<Notification> notis = notificationRepository.findByUserId(userId);
    for (Notification n : notis){
      n.update();
    }
  }

  @Override
  public NotificationDto updateStatus(UUID notificationId, UUID userId, NotificationUpdateRequest request) {
    if(userId == null){
      log.warn("잘못된 요청입니다.");
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }

    Users user = userRepository.findById(userId).orElseThrow(
        () -> {
          log.warn("사용자를 찾을 수 없습니다");
          return new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    );

    Notification notification = notificationRepository.findById(notificationId).
        orElseThrow(() -> {
          log.warn("알림을 찾을 수 없습니다");
          return new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND);
        });

    if(!notification.getUser().equals(user)){
      log.warn("알림에 대한 접근 권한이 없습니다.");
      throw new CustomException(ErrorCode.NOTIFICATION_ACCESS_FORBIDDEN);
    }

    notification.update();
    String message;

    if(notification.getType().equals(NotificationType.RANKING)){
      message = "당신의 리뷰가 인기 순위 10위 내에 선정되었습니다.";
    }
    else {
      String nickname = notification.getSender() != null ? notification.getSender().getNickname() : "알 수 없음";
      message = createMessage(notification.getType(), nickname);
    }

    return notificationMapper.toDto(notification, message);
  }

  public String createMessage(NotificationType type, String nickname){
    if(type.equals(NotificationType.LIKE)) {
      return "[" + nickname + "]님이 나의 리뷰를 좋아합니다.";
    }
    else {
      return "[" + nickname + "]님이 나의 리뷰에 댓글을 남겼습니다.";
    }
  }

  @Override
  public CursorPageResponseDto<NotificationDto> list(NotificationListRequest request) {
    return null;
  }
}
