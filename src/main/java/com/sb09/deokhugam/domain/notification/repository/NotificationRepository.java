package com.sb09.deokhugam.domain.notification.repository;

import com.sb09.deokhugam.domain.notification.entity.Notification;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
}
