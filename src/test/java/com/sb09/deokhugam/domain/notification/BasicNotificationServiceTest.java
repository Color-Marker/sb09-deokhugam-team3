package com.sb09.deokhugam.domain.notification;

import static org.mockito.Mockito.mock;

import com.sb09.deokhugam.domain.notification.entity.NotificationType;
import com.sb09.deokhugam.domain.notification.mapper.NotificationMapper;
import com.sb09.deokhugam.domain.notification.repository.NotificationRepository;
import com.sb09.deokhugam.domain.notification.service.NotificationService;
import com.sb09.deokhugam.domain.review.entity.Review;
import com.sb09.deokhugam.domain.user.entity.Users;
import com.sb09.deokhugam.domain.user.repository.UserRepository;
import com.sb09.deokhugam.global.common.mapper.CursorPageResponseMapper;
import static org.mockito.BDDMockito.given;

import java.util.UUID;
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
  private NotificationMapper notificationMapper;
  @Mock
  private CursorPageResponseMapper cursorPageResponseMapper;
  @InjectMocks
  private NotificationService notificationService;

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
    confirmStatus = false;
    type = NotificationType.LIKE;
    review = mock(Review.class);
    sender = mock(Users.class);
    user = mock(Users.class);

    given(user.getId()).willReturn(userId);
    given(sender.getId()).willReturn(senderId);
    given(review.getId()).willReturn(reviewId);
  }

  @Test
  @DisplayName("알람 생성 - 성공")
  void notification_create(){

  }

  @Test
  @DisplayName("알람 생성 - 사용자 조회 실패 예외")
  void notification_create_userNotFound(){

  }

  @Test
  @DisplayName("모든 알림 확인 - 성공")
  void notification_readAll(){

  }
  @Test
  @DisplayName("모든 알림 확인 - 잘못된 요청 예외")
  void notification_readAll_invalidRequest(){

  }

  @Test
  @DisplayName("모든 알림 확인 - 사용자 조회 실패 예외")
  void notification_readAll_userNotFound(){

  }

  @Test
  @DisplayName("특정 알림 확인 - 성공")
  void notification_read(){

  }

  @Test
  @DisplayName("특정 알림 확인 - 잘못된 요청 예외")
  void notification_read_invalidRequest(){

  }

  @Test
  @DisplayName("특정 알림 확인 - 사용자 조회 실패 예외")
  void notification_read_userNotFound(){

  }

  @Test
  @DisplayName("특정 알림 확인 - 알림 조회 실패 예외")
  void notification_read_notificationNotFound(){

  }

  @Test
  @DisplayName("특정 알림 확인 - 권한 없음 예외")
  void notification_read_forbidden(){

  }


}
