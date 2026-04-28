package com.sb09.deokhugam.global.exception.review;

import com.sb09.deokhugam.global.exception.ErrorCode;

public class InvalidReviewInputException extends ReviewException {

  public InvalidReviewInputException() {
    super(ErrorCode.VALIDATION_ERROR);
  }
}