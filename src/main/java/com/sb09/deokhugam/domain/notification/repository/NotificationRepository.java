package com.sb09.deokhugam.domain.notification.repository;

import com.sb09.deokhugam.domain.notification.entity.Notification;
import com.sb09.deokhugam.domain.user.entity.Users;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID>,
    NotificationRepositoryCustom {

  List<Notification> findByUserId(UUID userId);
}
