package com.sb09.deokhugam.global.Exception.review;

import com.sb09.deokhugam.global.Exception.CustomException;
import com.sb09.deokhugam.global.Exception.ErrorCode;

public class ReviewException extends CustomException {

  public ReviewException(ErrorCode errorCode) {
    super(errorCode);
  }

  public ReviewException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}