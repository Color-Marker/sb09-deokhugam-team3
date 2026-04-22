package com.sb09.deokhugam.domain.notification;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import com.sb09.deokhugam.domain.notification.dto.request.NotificationUpdateRequest;
import com.sb09.deokhugam.domain.notification.dto.response.NotificationDto;
import com.sb09.deokhugam.domain.notification.entity.Notification;
import com.sb09.deokhugam.domain.notification.entity.NotificationType;
import com.sb09.deokhugam.domain.notification.mapper.NotificationMapper;
import com.sb09.deokhugam.domain.notification.repository.NotificationRepository;
import com.sb09.deokhugam.domain.notification.service.basic.BasicNotificationService;
import com.sb09.deokhugam.domain.review.entity.Review;
import com.sb09.deokhugam.domain.review.repository.ReviewRepository;
import com.sb09.deokhugam.domain.user.entity.Users;
import com.sb09.deokhugam.domain.user.repository.UserRepository;
import com.sb09.deokhugam.global.Exception.CustomException;
import com.sb09.deokhugam.global.Exception.ErrorCode;
import com.sb09.deokhugam.global.Exception.comment.CommentAlreadyDeletedException;
import com.sb09.deokhugam.global.Exception.comment.ForbiddenAuthorityException;
import com.sb09.deokhugam.global.Exception.notification.NotificationForbiddenException;
import com.sb09.deokhugam.global.Exception.notification.NotificationNotFoundException;
import com.sb09.deokhugam.global.Exception.user.UserNotFoundException;
import com.sb09.deokhugam.global.common.mapper.CursorPageResponseMapper;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BasicNotificationServiceTest {
  @Mock
  private NotificationRepository notificationRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private ReviewRepository reviewRepository;
  @Mock
  private NotificationMapper notificationMapper;
  @Mock
  private CursorPageResponseMapper cursorPageResponseMapper;
  @InjectMocks
  private BasicNotificationService notificationService;

  private boolean confirmStatus;
  private NotificationType type;
  private Review review;
  private Users sender;
  private Users user;
  private UUID reviewId;
  private UUID senderId;
  private UUID userId;

  @BeforeEach
  void setUp(){
    userId = UUID.randomUUID();
    senderId = UUID.randomUUID();
    reviewId = UUID.randomUUID();

    type = NotificationType.LIKE;
    review = mock(Review.class);
    sender = mock(Users.class);
    user = mock(Users.class);

    given(user.getId()).willReturn(userId);
    given(sender.getId()).willReturn(senderId);
    given(review.getUserId()).willReturn(userId);
    given(review.getId()).willReturn(reviewId);
    given(user.getDeletedAt()).willReturn(null);
  }

  @Test
  @DisplayName("알람 생성 - 성공")
  void notification_create(){
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(reviewRepository.existsByIdAndDeletedAtIsNull(reviewId)).willReturn(true);
    Notification savedNotification = new Notification(type, review, sender, user);
    given(notificationRepository.save(any(Notification.class))).willReturn(savedNotification);

    Notification result = notificationService.create(type, review, sender);

    assertThat(result).isNotNull();
    assertThat(result.getType()).isEqualTo(type);
    verify(notificationRepository, times(1)).save(any(Notification.class));
  }

  @Test
  @DisplayName("알람 생성 - 사용자 조회 실패 예외")
  void notification_create_userNotFound(){
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> notificationService.create(type, review, sender))
        .isInstanceOf(UserNotFoundException.class)
        .satisfies(e -> Assertions.assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.USER_NOT_FOUND));

  }

  @Test
  @DisplayName("모든 알림 확인 - 성공")
  void notification_readAll(){
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(user.getDeletedAt()).willReturn(null);
    Notification noti1 = new Notification(type, review, sender, user);
    Notification noti2 = new Notification(type, review, sender, user);
    given(notificationRepository.findByUserId(userId)).willReturn(List.of(noti1, noti2));

    notificationService.readAll(userId);

    assertThat(noti1.getConfirmStatus()).isTrue();
    assertThat(noti2.getConfirmStatus()).isTrue();
  }
  @Test
  @DisplayName("모든 알림 확인 - 잘못된 요청 예외")
  void notification_readAll_invalidRequest(){
    assertThatThrownBy(() -> notificationService.readAll(null))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);
  }

  @Test
  @DisplayName("모든 알림 확인 - 사용자 조회 실패 예외")
  void notification_readAll_userNotFound(){
    given(userRepository.existsById(userId)).willReturn(false);

    assertThatThrownBy(() -> notificationService.readAll(userId))
        .isInstanceOf(UserNotFoundException.class)
        .satisfies(e -> Assertions.assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.USER_NOT_FOUND));
  }

  @Test
  @DisplayName("특정 알림 확인 - 성공")
  void notification_read(){
    UUID notificationId = UUID.randomUUID();
    Notification notification = new Notification(type, review, sender, user);
    NotificationUpdateRequest request = new NotificationUpdateRequest(true);
    NotificationDto responseDto = new NotificationDto(
        notificationId, userId, reviewId, "리뷰 내용", "메시지", true, LocalDateTime.now(), LocalDateTime.now()
    );

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));
    given(notificationMapper.toDto(any(Notification.class))).willReturn(responseDto);

    NotificationDto result = notificationService.updateStatus(notificationId, userId, request);

    assertThat(notification.getConfirmStatus()).isTrue();
    assertThat(result.confirmed()).isTrue();
  }

  @Test
  @DisplayName("특정 알림 확인 - 잘못된 요청 예외")
  void notification_read_invalidRequest(){
    assertThatThrownBy(() -> notificationService.updateStatus(UUID.randomUUID(), null, null))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);
  }

  @Test
  @DisplayName("특정 알림 확인 - 사용자 조회 실패 예외")
  void notification_read_userNotFound(){
    NotificationUpdateRequest request = new NotificationUpdateRequest(true);
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> notificationService.updateStatus(UUID.randomUUID(), userId, request))
        .isInstanceOf(UserNotFoundException.class)
        .satisfies(e -> Assertions.assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.USER_NOT_FOUND));
  }

  @Test
  @DisplayName("특정 알림 확인 - 알림 조회 실패 예외")
  void notification_read_notificationNotFound(){
    UUID notificationId = UUID.randomUUID();
    NotificationUpdateRequest request = new NotificationUpdateRequest(true);

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(notificationRepository.findById(notificationId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> notificationService.updateStatus(notificationId, userId, request))
        .isInstanceOf(NotificationNotFoundException.class)
        .satisfies(e -> Assertions.assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND));

  }

  @Test
  @DisplayName("특정 알림 확인 - 권한 없음 예외")
  void notification_read_forbidden(){
    UUID notificationId = UUID.randomUUID();
    Users anotherUser = mock(Users.class);
    Notification notification = new Notification(type, review, sender, anotherUser);
    NotificationUpdateRequest request = new NotificationUpdateRequest(true); // 추가

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));

    assertThatThrownBy(() -> notificationService.updateStatus(notificationId, userId, request))
        .isInstanceOf(NotificationForbiddenException.class)
        .satisfies(e -> Assertions.assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.NOTIFICATION_ACCESS_FORBIDDEN));
  }
}
