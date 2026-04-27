package com.sb09.deokhugam.domain.notification.repository;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.sb09.deokhugam.domain.config.QueryDslTestConfig;
import com.sb09.deokhugam.domain.notification.dto.request.NotificationListRequest;
import com.sb09.deokhugam.domain.notification.entity.Notification;
import com.sb09.deokhugam.domain.notification.entity.NotificationType;
import com.sb09.deokhugam.domain.user.entity.Users;
import com.sb09.deokhugam.domain.user.repository.UserRepository;
import com.sb09.deokhugam.global.common.entity.BaseEntity;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

@DataJpaTest
@Import(QueryDslTestConfig.class)
public class NotificationRepositoryTest {
  @Autowired
  private NotificationRepository notificationRepository;
  @Autowired
  private UserRepository userRepository;

  private Users user1;
  private Users user2;

  @BeforeEach
  void setUp() {
    user1 = userRepository.save(Users.builder()
            .email("test1@test.com")
            .nickname("user1")
            .password("password1")
        .build());
    user2 = userRepository.save(Users.builder()
        .email("test2@test.com")
        .nickname("user2")
        .password("password2")
        .build());
  }

  @Test
  void findNotificationByUserId(){
    notificationRepository.save(new Notification(NotificationType.LIKE, null, user2, user1));
    notificationRepository.save(new Notification(NotificationType.LIKE, null, user1, user2));
    List<Notification> result = notificationRepository.findByUserId(user1.getId());
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getUser().getId()).isEqualTo(user1.getId());

  }

  @Test
  void returnEmptyListIfNoNotification(){
    List<Notification> result = notificationRepository.findByUserId(user1.getId());

    assertThat(result).isEmpty();
  }

  @Test
  void NoUserIdExists() {
    notificationRepository.save(new Notification(NotificationType.LIKE, null, user2, user1));

    List<Notification> result = notificationRepository.findByUserId(UUID.randomUUID());

    assertThat(result).isEmpty();
  }

  @Test
  void firstPageWithoutCursor() {
    for (int i = 0; i < 5; i++) {
      notificationRepository.save(new Notification(NotificationType.LIKE, null, user2, user1));
    }

    NotificationListRequest request = new NotificationListRequest();
    request.setUserId(user1.getId());
    request.setLimit(3);

    Slice<Notification> result = notificationRepository.searchNotification(request);

    assertThat(result.getContent()).hasSize(3);
    assertThat(result.hasNext()).isTrue();
  }

  @Test
  void finalPageHasNextFalse() {
    for (int i = 0; i < 2; i++) {
      notificationRepository.save(new Notification(NotificationType.LIKE, null, user2, user1));
    }

    NotificationListRequest request = new NotificationListRequest();
    request.setUserId(user1.getId());
    request.setLimit(3);

    Slice<Notification> result = notificationRepository.searchNotification(request);

    assertThat(result.getContent()).hasSize(2);
    assertThat(result.hasNext()).isFalse();
  }

  @Test
  void sortByDescSuccess() throws InterruptedException {
    Notification first = notificationRepository.save(
        new Notification(NotificationType.LIKE, null, user2, user1));
    Thread.sleep(100);
    Notification second = notificationRepository.save(
        new Notification(NotificationType.LIKE, null, user2, user1));

    NotificationListRequest request = new NotificationListRequest();
    request.setUserId(user1.getId());
    request.setLimit(100);
    request.setDirection(Sort.Direction.DESC);

    Slice<Notification> result = notificationRepository.searchNotification(request);

    assertThat(result.getContent().get(0).getId()).isEqualTo(second.getId());
    assertThat(result.getContent().get(1).getId()).isEqualTo(first.getId());
  }

  @Test
  void noSelectOtherUserNotification() {
    notificationRepository.save(new Notification(NotificationType.LIKE, null, user2, user1));
    notificationRepository.save(new Notification(NotificationType.LIKE, null, user1, user2));

    NotificationListRequest request = new NotificationListRequest();
    request.setUserId(user1.getId());
    request.setLimit(100);

    Slice<Notification> result = notificationRepository.searchNotification(request);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent()).allMatch(n -> n.getUser().getId().equals(user1.getId()));
  }
  @Test
  void returnCountNotiFromUser() {
    notificationRepository.save(new Notification(NotificationType.LIKE, null, user2, user1));
    notificationRepository.save(new Notification(NotificationType.LIKE, null, user2, user1));
    notificationRepository.save(new Notification(NotificationType.LIKE, null, user1, user2)); // userB 알림

    NotificationListRequest request = new NotificationListRequest();
    request.setUserId(user1.getId());

    Long count = notificationRepository.countNotification(request);

    assertThat(count).isEqualTo(2);
  }

  @Test
  void DeleteConfirmedOldNoti() {
    Notification old = notificationRepository.save(
        new Notification(NotificationType.LIKE, null, user2, user1));
    old.update();
    notificationRepository.save(old);

    long deleted = notificationRepository.deleteOldNotification(LocalDateTime.now().plusSeconds(1));

    assertThat(deleted).isEqualTo(1);
  }

  @Test
  void NoDeleteUnConfirmedNoti() {
    notificationRepository.save(new Notification(NotificationType.LIKE, null, user2, user1));

    long deleted = notificationRepository.deleteOldNotification(LocalDateTime.now().plusSeconds(1));

    assertThat(deleted).isEqualTo(0);
  }

  @Test
  void NoDeleteRecentNoti() {
    Notification recent = notificationRepository.save(
        new Notification(NotificationType.LIKE, null, user2, user1));
    recent.update();
    notificationRepository.save(recent);

    long deleted = notificationRepository.deleteOldNotification(LocalDateTime.now().minusDays(1));

    assertThat(deleted).isEqualTo(0);
  }

}
