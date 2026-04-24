package com.sb09.deokhugam.global.exception.notification;

import com.sb09.deokhugam.global.exception.ErrorCode;
import java.util.UUID;

public class NotificationNotFoundException extends NotificationException {

  public NotificationNotFoundException() {
    super(ErrorCode.NOTIFICATION_NOT_FOUND);
  }

  public static NotificationNotFoundException withId(UUID notificationId) {
    NotificationNotFoundException exception = new NotificationNotFoundException();
    exception.addDetail("notificationId", notificationId);
    return exception;
  }
}
