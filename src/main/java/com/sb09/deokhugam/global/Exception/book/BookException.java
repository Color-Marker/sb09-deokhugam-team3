package com.sb09.deokhugam.global.Exception.book;

import com.sb09.deokhugam.global.Exception.CustomException;
import com.sb09.deokhugam.global.Exception.ErrorCode;

public class BookException extends CustomException {

  public BookException(ErrorCode errorCode) {
    super(errorCode);
  }

  public BookException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
