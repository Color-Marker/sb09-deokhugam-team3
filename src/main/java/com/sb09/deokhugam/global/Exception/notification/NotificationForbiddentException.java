package com.sb09.deokhugam.global.Exception.notification;

import com.sb09.deokhugam.global.Exception.ErrorCode;

public class NotificationForbiddentException extends NotificationException{

  public NotificationForbiddentException(ErrorCode errorCode) {
    super(errorCode);
  }

}
