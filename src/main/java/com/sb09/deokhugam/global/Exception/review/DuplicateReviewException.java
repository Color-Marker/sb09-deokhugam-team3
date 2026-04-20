package com.sb09.deokhugam.global.Exception.review;

import com.sb09.deokhugam.global.Exception.ErrorCode;

public class DuplicateReviewException extends ReviewException {

  public DuplicateReviewException() {
    super(ErrorCode.DUPLICATE_REVIEW);
  }
}