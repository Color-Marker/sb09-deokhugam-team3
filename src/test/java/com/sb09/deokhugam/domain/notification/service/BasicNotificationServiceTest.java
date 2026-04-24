package com.sb09.deokhugam.domain.notification.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.sb09.deokhugam.domain.notification.dto.request.NotificationListRequest;
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
import com.sb09.deokhugam.global.exception.CustomException;
import com.sb09.deokhugam.global.exception.ErrorCode;
import com.sb09.deokhugam.global.exception.notification.NotificationForbiddenException;
import com.sb09.deokhugam.global.exception.notification.NotificationNotFoundException;
import com.sb09.deokhugam.global.exception.user.UserNotFoundException;
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
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

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
  void setUp() {
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
  void notification_create() {
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
  void notification_create_userNotFound() {
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> notificationService.create(type, review, sender))
        .isInstanceOf(UserNotFoundException.class)
        .satisfies(e -> Assertions.assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.USER_NOT_FOUND));

  }

  @Test
  @DisplayName("알람 생성 - 삭제된 사용자 조회 예외")
  void notification_create_deletedUser(){
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(user.getDeletedAt()).willReturn(LocalDateTime.now());

    assertThatThrownBy(() -> notificationService.create(type, review, sender))
        .isInstanceOf(UserAlreadyDeletedException.class)
        .satisfies(e -> Assertions.assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.DELETED_USER));

  }

  @Test
  @DisplayName("알람 생성 - 리뷰 조회 실패 예외")
  void notification_create_reviewNotFound(){
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(user.getDeletedAt()).willReturn(null);
    given(reviewRepository.existsByIdAndDeletedAtIsNull(reviewId)).willReturn(false);

    assertThatThrownBy(() -> notificationService.create(type, review, sender))
        .isInstanceOf(ReviewNotFoundException.class)
        .satisfies(e -> Assertions.assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.REVIEW_NOT_FOUND));

  }

  @Test
  @DisplayName("알람 생성 - 본인에게는 알람 생성 안 함 (null 반환)")
  void notification_create_selfNotification() {
    given(sender.getId()).willReturn(userId);
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(reviewRepository.existsByIdAndDeletedAtIsNull(reviewId)).willReturn(true);

    Notification result = notificationService.create(type, review, sender);

    assertThat(result).isNull();
    verify(notificationRepository, times(0)).save(any(Notification.class));
  }

  @Test
  @DisplayName("알람 생성 - RANKING 타입 sender null 성공")
  void notification_create_rankingType() {
    type = NotificationType.RANKING;
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(reviewRepository.existsByIdAndDeletedAtIsNull(reviewId)).willReturn(true);
    Notification savedNotification = new Notification(type, review, null, user);
    given(notificationRepository.save(any(Notification.class))).willReturn(savedNotification);

    Notification result = notificationService.create(type, review, null);

    assertThat(result).isNotNull();
    assertThat(result.getType()).isEqualTo(NotificationType.RANKING);
    verify(notificationRepository, times(1)).save(any(Notification.class));
  }

  @Test
  @DisplayName("모든 알림 확인 - 성공")
  void notification_readAll() {
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
  void notification_readAll_invalidRequest() {
    assertThatThrownBy(() -> notificationService.readAll(null))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);
  }

  @Test
  @DisplayName("모든 알림 확인 - 사용자 조회 실패 예외")
  void notification_readAll_userNotFound() {
    given(userRepository.existsById(userId)).willReturn(false);

    assertThatThrownBy(() -> notificationService.readAll(userId))
        .isInstanceOf(UserNotFoundException.class)
        .satisfies(e -> Assertions.assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.USER_NOT_FOUND));
  }

  @Test
  @DisplayName("모든 알림 확인 - 삭제된 사용자 예외")
  void notification_readAll_deletedUser() {
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(user.getDeletedAt()).willReturn(LocalDateTime.now());

    assertThatThrownBy(() -> notificationService.readAll(userId))
        .isInstanceOf(UserAlreadyDeletedException.class)
        .satisfies(e -> Assertions.assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.DELETED_USER));
  }

  @Test
  @DisplayName("특정 알림 확인 - 성공")
  void notification_read() {
    UUID notificationId = UUID.randomUUID();
    Notification notification = new Notification(type, review, sender, user);
    NotificationUpdateRequest request = new NotificationUpdateRequest(true);
    NotificationDto responseDto = new NotificationDto(
        notificationId, userId, reviewId, "리뷰 내용", "메시지", true, LocalDateTime.now(),
        LocalDateTime.now()
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
  void notification_read_invalidRequest() {
    assertThatThrownBy(() -> notificationService.updateStatus(UUID.randomUUID(), null, null))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);
  }

  @Test
  @DisplayName("특정 알림 확인 - confirmed=false 잘못된 요청 예외")
  void notification_read_invalidRequest_confirmedFalse() {
    NotificationUpdateRequest request = new NotificationUpdateRequest(false);

    assertThatThrownBy(() -> notificationService.updateStatus(UUID.randomUUID(), userId, request))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);
  }

  @Test
  @DisplayName("특정 알림 확인 - 사용자 조회 실패 예외")
  void notification_read_userNotFound() {
    NotificationUpdateRequest request = new NotificationUpdateRequest(true);
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> notificationService.updateStatus(UUID.randomUUID(), userId, request))
        .isInstanceOf(UserNotFoundException.class)
        .satisfies(e -> Assertions.assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.USER_NOT_FOUND));
  }

  @Test
  @DisplayName("특정 알림 확인 - 삭제된 사용자 예외")
  void notification_read_deletedUser() {
    NotificationUpdateRequest request = new NotificationUpdateRequest(true);
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(user.getDeletedAt()).willReturn(LocalDateTime.now());

    assertThatThrownBy(() -> notificationService.updateStatus(UUID.randomUUID(), userId, request))
        .isInstanceOf(UserAlreadyDeletedException.class)
        .satisfies(e -> Assertions.assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.DELETED_USER));
  }

  @Test
  @DisplayName("특정 알림 확인 - 알림 조회 실패 예외")
  void notification_read_notificationNotFound() {
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
  void notification_read_forbidden() {
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

  @Test
  @DisplayName("알림 목록 조회 - 성공")
  void notification_list_success() {
    // given
    NotificationListRequest request = mock(NotificationListRequest.class);
    given(request.getUserId()).willReturn(userId);
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(user.getDeletedAt()).willReturn(null);

    Notification noti1 = new Notification(type, review, sender, user);
    Notification noti2 = new Notification(type, review, sender, user);

    Slice<Notification> slice = new SliceImpl<>(List.of(noti1, noti2));
    given(notificationRepository.searchNotification(request)).willReturn(slice);
    given(notificationRepository.countNotification(request)).willReturn(2L);

    NotificationDto dto1 = new NotificationDto(
        UUID.randomUUID(), userId, reviewId, "리뷰1", "메시지1", false, LocalDateTime.now(), LocalDateTime.now()
    );
    NotificationDto dto2 = new NotificationDto(
        UUID.randomUUID(), userId, reviewId, "리뷰2", "메시지2", false, LocalDateTime.now(), LocalDateTime.now()
    );

    CursorPageResponseDto<NotificationDto> expectedResponse = new CursorPageResponseDto<>(
        List.of(dto1, dto2), null, null, 2, 2L, false
    );
    doReturn(expectedResponse)
        .when(cursorPageResponseMapper)
        .fromSlice(any(), any(), any(), any(), eq(2L));

    // when
    CursorPageResponseDto<NotificationDto> result = notificationService.list(request);

    // then
    assertThat(result).isNotNull();
    assertThat(result.totalElements()).isEqualTo(2L);
    assertThat(result.size()).isEqualTo(2);
    assertThat(result.hasNext()).isFalse();
    verify(notificationRepository, times(1)).searchNotification(request);
    verify(notificationRepository, times(1)).countNotification(request);
  }

  @Test
  @DisplayName("알림 목록 조회 - 사용자 조회 실패 예외")
  void notification_list_userNotFound() {
    NotificationListRequest request = mock(NotificationListRequest.class);
    given(request.getUserId()).willReturn(userId);
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> notificationService.list(request))
        .isInstanceOf(UserNotFoundException.class)
        .satisfies(e -> Assertions.assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.USER_NOT_FOUND));
  }

  @Test
  @DisplayName("알림 목록 조회 - 삭제된 사용자 예외")
  void notification_list_deletedUser() {
    NotificationListRequest request = mock(NotificationListRequest.class);
    given(request.getUserId()).willReturn(userId);
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(user.getDeletedAt()).willReturn(LocalDateTime.now());

    assertThatThrownBy(() -> notificationService.list(request))
        .isInstanceOf(UserAlreadyDeletedException.class)
        .satisfies(e -> Assertions.assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.DELETED_USER));
  }
}
