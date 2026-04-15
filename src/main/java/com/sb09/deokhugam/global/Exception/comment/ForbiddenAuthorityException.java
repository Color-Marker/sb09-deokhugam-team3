package com.sb09.deokhugam.global.Exception.comment;

import com.sb09.deokhugam.global.Exception.ErrorCode;

public class ForbiddenAuthorityException extends CommentException {

  public ForbiddenAuthorityException(ErrorCode errorCode) {
    super(errorCode);
  }
}
