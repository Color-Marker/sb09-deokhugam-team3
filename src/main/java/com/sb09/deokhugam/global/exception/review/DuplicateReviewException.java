package com.sb09.deokhugam.global.exception.review;

import com.sb09.deokhugam.global.exception.ErrorCode;

public class DuplicateReviewException extends ReviewException {

  public DuplicateReviewException() {
    super(ErrorCode.DUPLICATE_REVIEW);
  }
}