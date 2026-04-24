package com.sb09.deokhugam.global.exception.book;

import com.sb09.deokhugam.global.exception.ErrorCode;
import java.util.UUID;

public class BookNotFoundException extends BookException {

  public BookNotFoundException() {
    super(ErrorCode.BOOK_NOT_FOUND);
  }

  public static BookNotFoundException withId(UUID bookId) {
    BookNotFoundException exception = new BookNotFoundException();
    exception.addDetail("bookId", bookId);
    return exception;
  }

}
