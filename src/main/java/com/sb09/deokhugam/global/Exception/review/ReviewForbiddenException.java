package com.sb09.deokhugam.global.Exception.review;

import com.sb09.deokhugam.global.Exception.ErrorCode;

public class ReviewForbiddenException extends ReviewException {

  public ReviewForbiddenException() {
    super(ErrorCode.UNAUTHORIZED_ACCESS);
  }
}