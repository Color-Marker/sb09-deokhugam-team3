package com.sb09.deokhugam.global.exception.user;

import com.sb09.deokhugam.global.exception.ErrorCode;
import java.util.UUID;

public class UserNotFoundException extends UserException {

  public UserNotFoundException() {
    super(ErrorCode.USER_NOT_FOUND);
  }

  public static UserNotFoundException withId(UUID userId) {
    UserNotFoundException exception = new UserNotFoundException();
    exception.addDetail("userId", userId);
    return exception;
  }

}
