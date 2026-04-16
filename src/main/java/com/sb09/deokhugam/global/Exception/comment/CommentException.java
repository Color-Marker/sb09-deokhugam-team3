package com.sb09.deokhugam.global.Exception.comment;

import com.sb09.deokhugam.global.Exception.CustomException;
import com.sb09.deokhugam.global.Exception.ErrorCode;

public class CommentException extends CustomException {

  public CommentException(ErrorCode errorCode) {
    super(errorCode);
  }

  public CommentException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
