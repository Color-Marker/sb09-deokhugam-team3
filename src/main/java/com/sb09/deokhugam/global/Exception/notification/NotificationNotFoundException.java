package com.sb09.deokhugam.global.Exception.notification;

import com.sb09.deokhugam.global.Exception.ErrorCode;
import java.util.UUID;

public class NotificationNotFoundException extends NotificationException{

  public NotificationNotFoundException() {
    super(ErrorCode.NOTIFICATION_NOT_FOUND);
  }

  public static NotificationNotFoundException withId(UUID notificationId) {
    NotificationNotFoundException exception = new NotificationNotFoundException();
    exception.addDetail("notificationId", notificationId);
    return exception;
  }
}
