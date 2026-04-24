package com.sb09.deokhugam.global.exception.review;

import com.sb09.deokhugam.global.exception.ErrorCode;

public class ReviewForbiddenException extends ReviewException {

  public ReviewForbiddenException() {
    super(ErrorCode.UNAUTHORIZED_ACCESS);
  }
}