package com.sb09.deokhugam.global.exception.review;

import com.sb09.deokhugam.global.exception.ErrorCode;
import java.util.UUID;

public class ReviewNotFoundException extends ReviewException {

  public ReviewNotFoundException() {
    super(ErrorCode.REVIEW_NOT_FOUND);
  }

  public static ReviewNotFoundException withId(UUID reviewId) {
    ReviewNotFoundException exception = new ReviewNotFoundException();
    exception.addDetail("reviewId", reviewId);
    return exception;
  }
}