package com.sb09.deokhugam.global.Exception.book;

import com.sb09.deokhugam.global.Exception.ErrorCode;

public class DuplicateIsbnException extends BookException {

  public DuplicateIsbnException() {
    super(ErrorCode.DUPLICATE_ISBN);
  }

  public static DuplicateIsbnException withIsbn(String isbn) {
    DuplicateIsbnException exception = new DuplicateIsbnException();
    exception.addDetail("isbn", isbn);
    return exception;
  }
}
