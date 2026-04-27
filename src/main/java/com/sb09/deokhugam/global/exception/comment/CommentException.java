package com.sb09.deokhugam.global.exception.comment;

import com.sb09.deokhugam.global.exception.CustomException;
import com.sb09.deokhugam.global.exception.ErrorCode;

public class CommentException extends CustomException {

  public CommentException(ErrorCode errorCode) {
    super(errorCode);
  }

  public CommentException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
