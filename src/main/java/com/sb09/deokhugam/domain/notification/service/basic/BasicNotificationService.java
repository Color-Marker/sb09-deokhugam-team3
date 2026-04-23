package com.sb09.deokhugam.domain.notification.service.basic;

import com.sb09.deokhugam.domain.notification.dto.request.NotificationListRequest;
import com.sb09.deokhugam.domain.notification.dto.request.NotificationUpdateRequest;
import com.sb09.deokhugam.domain.notification.dto.response.NotificationDto;
import com.sb09.deokhugam.domain.notification.entity.Notification;
import com.sb09.deokhugam.domain.notification.entity.NotificationType;
import com.sb09.deokhugam.domain.notification.mapper.NotificationMapper;
import com.sb09.deokhugam.domain.notification.repository.NotificationRepository;
import com.sb09.deokhugam.domain.notification.service.NotificationService;
import com.sb09.deokhugam.domain.review.entity.Review;
import com.sb09.deokhugam.domain.review.repository.ReviewRepository;
import com.sb09.deokhugam.domain.user.entity.Users;
import com.sb09.deokhugam.domain.user.repository.UserRepository;
import com.sb09.deokhugam.global.Exception.CustomException;
import com.sb09.deokhugam.global.Exception.ErrorCode;
import com.sb09.deokhugam.global.Exception.notification.NotificationForbiddenException;
import com.sb09.deokhugam.global.Exception.notification.NotificationNotFoundException;
import com.sb09.deokhugam.global.Exception.review.ReviewNotFoundException;
import com.sb09.deokhugam.global.Exception.user.UserAlreadyDeletedException;
import com.sb09.deokhugam.global.Exception.user.UserNotFoundException;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import com.sb09.deokhugam.global.common.mapper.CursorPageResponseMapper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BasicNotificationService implements NotificationService {
  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;
  private final ReviewRepository reviewRepository;
  private final NotificationMapper notificationMapper;
  private final CursorPageResponseMapper cursorPageResponseMapper;

  @Override
  public void readAll(UUID userId) {
    if(userId == null){
      log.warn("잘못된 요청입니다.");
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }

    Users user = userRepository.findById(userId).orElseThrow(
        () -> {
          log.warn("사용자를 찾을 수 없습니다");
          return UserNotFoundException.withId(userId);
        }
    );
    if(user.getDeletedAt() != null){
      log.warn("탈퇴한 사용자이므로 알람을 생성할 수 없습니다.");
      throw UserAlreadyDeletedException.withId(userId);
    }
    log.info("유저ID: {} 의 모든 알림을 읽음 상태로 전환합니다.", userId);
    List<Notification> notis = notificationRepository.findByUserId(userId);
    for (Notification n : notis){
      log.info("알람ID: {} 을 읽음 상태로 전환합니다.", n.getId());
      n.update();
    }
    log.info("유저ID: {} 의 모든 알림 읽음 상태 전환 완료되었습니다.", userId);
  }

  @Override
  public NotificationDto updateStatus(UUID notificationId, UUID userId, NotificationUpdateRequest request) {
    if(userId == null || request.confirmed() != true){
      log.warn("잘못된 요청입니다.");
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }

    Users user = userRepository.findById(userId).orElseThrow(
        () -> {
          log.warn("사용자를 찾을 수 없습니다");
          return UserNotFoundException.withId(userId);
        }
    );
    if(user.getDeletedAt() != null){
      log.warn("탈퇴한 사용자이므로 알람을 생성할 수 없습니다.");
      throw UserAlreadyDeletedException.withId(userId);
    }

    Notification notification = notificationRepository.findById(notificationId).
        orElseThrow(() -> {
          log.warn("알림을 찾을 수 없습니다");
          return NotificationNotFoundException.withId(notificationId);
        });

    if(!notification.getUser().equals(user)){
      log.warn("알림에 대한 접근 권한이 없습니다.");
      throw new NotificationForbiddenException(ErrorCode.NOTIFICATION_ACCESS_FORBIDDEN);
    }

    if(notification.getConfirmStatus()){
      log.info("이미 확인한 알람입니다.");
    }
    else {
      log.info("유저 {}의 알림 {}을 읽음 상태로 전환합니다.", user.getNickname(), notification.getId());
      notification.update();
    }
    return notificationMapper.toDto(notification);
  }

  @Transactional(readOnly = true)
  @Override
  public CursorPageResponseDto<NotificationDto> list(NotificationListRequest request) {
    Users user = userRepository.findById(request.getUserId()).orElseThrow(
        () -> {
          log.warn("사용자를 찾을 수 없습니다");
          return UserNotFoundException.withId(request.getUserId());
        }
    );
    if(user.getDeletedAt() != null){
      log.warn("탈퇴한 사용자이므로 알람을 생성할 수 없습니다.");
      throw UserAlreadyDeletedException.withId(user.getId());
    }
    Slice<Notification> slice = notificationRepository.searchNotification(request);
    Long totalElements = notificationRepository.countNotification(request);
    log.info("유저ID: {} 의 알림 목록을 불러옵니다.", request.getUserId());
    log.info("유저ID {} 의 알림 {} 개를 불러옵니다.", request.getUserId(), totalElements);
    return cursorPageResponseMapper.fromSlice(
        slice,
        notificationMapper::toDto,
        Notification::getId,
        Notification::getCreatedAt,
        totalElements
    );
  }

  // 위는 controller에서 호출 -----------------------------------
  // 아래는 내부 작업 -----------------------------------
  @Override
  public Notification create(NotificationType type, Review review, Users sender){
    UUID userId = review.getUserId();
    Users user = userRepository.findById(userId).orElseThrow(
        () -> {
          log.warn("사용자를 찾을 수 없습니다");
          return UserNotFoundException.withId(userId);
        }
    );
    if(user.getDeletedAt() != null){
      log.warn("탈퇴한 사용자이므로 알람을 생성할 수 없습니다.");
      throw UserAlreadyDeletedException.withId(userId);
    }
    if(!reviewRepository.existsByIdAndDeletedAtIsNull(review.getId())){
      log.warn("리뷰를 찾을 수 없습니다.");
      throw ReviewNotFoundException.withId(review.getId());
    }

    Notification notification;
    if(!type.equals(NotificationType.RANKING) && sender != null){
      if(userId.equals(sender.getId())){
        log.info("본인 스스로에게는 알람을 생성하지 않습니다.");
        return null;
      }
    }
      // 좋아요 & 댓글
      notification = new Notification(type, review, sender, user);

    Notification result = notificationRepository.save(notification);
    log.info("타입: {} 인 알람: {} 이 생성되었습니다.", result.getType().toString(), result.getId());
    return result;
  }

}
