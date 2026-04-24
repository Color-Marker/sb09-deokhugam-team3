package com.sb09.deokhugam.global.exception.book;

import com.sb09.deokhugam.global.exception.CustomException;
import com.sb09.deokhugam.global.exception.ErrorCode;

public class BookException extends CustomException {

  public BookException(ErrorCode errorCode) {
    super(errorCode);
  }

  public BookException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
