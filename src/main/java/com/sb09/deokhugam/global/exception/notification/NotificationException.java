package com.sb09.deokhugam.global.exception.notification;

import com.sb09.deokhugam.global.exception.CustomException;
import com.sb09.deokhugam.global.exception.ErrorCode;

public class NotificationException extends CustomException {

  public NotificationException(ErrorCode errorCode) {
    super(errorCode);
  }

  public NotificationException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
