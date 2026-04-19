package com.sb09.deokhugam.global.Exception.user;

import com.sb09.deokhugam.global.Exception.ErrorCode;

public class DuplicateEmailException extends UserException {

  public DuplicateEmailException() {
    super(ErrorCode.DUPLICATE_EMAIL);
  }

  public static DuplicateEmailException withEmail(String email) {
    DuplicateEmailException exception = new DuplicateEmailException();
    exception.addDetail("email", email);
    return exception;
  }

}
