package com.sb09.deokhugam.global.exception.comment;

import com.sb09.deokhugam.global.exception.ErrorCode;

public class ForbiddenAuthorityException extends CommentException {

  public ForbiddenAuthorityException(ErrorCode errorCode) {
    super(errorCode);
  }
}
