package com.sb09.deokhugam.global.exception.review;

import com.sb09.deokhugam.global.exception.CustomException;
import com.sb09.deokhugam.global.exception.ErrorCode;

public class ReviewException extends CustomException {

  public ReviewException(ErrorCode errorCode) {
    super(errorCode);
  }

  public ReviewException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}