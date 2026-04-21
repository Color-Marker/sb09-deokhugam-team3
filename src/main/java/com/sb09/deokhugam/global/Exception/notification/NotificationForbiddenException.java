package com.sb09.deokhugam.global.Exception.notification;

import com.sb09.deokhugam.global.Exception.ErrorCode;

public class NotificationForbiddenException extends NotificationException{

  public NotificationForbiddenException(ErrorCode errorCode) {
    super(errorCode);
  }

}
