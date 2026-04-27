package com.sb09.deokhugam.global.exception.user;

import com.sb09.deokhugam.global.exception.CustomException;
import com.sb09.deokhugam.global.exception.ErrorCode;

public class UserException extends CustomException {

  public UserException(ErrorCode errorCode) {
    super(errorCode);
  }

  //Throwable는 어떤 예외든 담을 수 있는 타입임
  public UserException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

}
