package com.sb09.deokhugam.global.exception.review;

import com.sb09.deokhugam.global.exception.ErrorCode;
import java.util.UUID;

public class ReviewAlreadyDeletedException extends ReviewException {

  public ReviewAlreadyDeletedException() {
    super(ErrorCode.DELETED_REVIEW);
  }

  public static ReviewAlreadyDeletedException withId(UUID reviewId) {
    ReviewAlreadyDeletedException exception = new ReviewAlreadyDeletedException();
    exception.addDetail("reviewId", reviewId);
    return exception;
  }
}