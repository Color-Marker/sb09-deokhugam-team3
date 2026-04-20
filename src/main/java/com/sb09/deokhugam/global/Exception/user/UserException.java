package com.sb09.deokhugam.global.Exception.user;

import com.sb09.deokhugam.global.Exception.CustomException;
import com.sb09.deokhugam.global.Exception.ErrorCode;

public class UserException extends CustomException {

  public UserException(ErrorCode errorCode) {
    super(errorCode);
  }

  //Throwable는 어떤 예외든 담을 수 있는 타입임
  public UserException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

}
