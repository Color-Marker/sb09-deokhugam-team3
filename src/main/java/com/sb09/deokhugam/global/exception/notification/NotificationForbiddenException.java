package com.sb09.deokhugam.global.exception.notification;

import com.sb09.deokhugam.global.exception.ErrorCode;

public class NotificationForbiddenException extends NotificationException {

  public NotificationForbiddenException(ErrorCode errorCode) {
    super(errorCode);
  }

}
