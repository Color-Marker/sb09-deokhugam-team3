package com.sb09.deokhugam.global.exception.user;

import com.sb09.deokhugam.global.exception.ErrorCode;
import java.util.UUID;

public class UserAlreadyDeletedException extends UserException {

  public UserAlreadyDeletedException() {
    super(ErrorCode.DELETED_USER);
  }

  public static UserAlreadyDeletedException withId(UUID id) {
    UserAlreadyDeletedException exception = new UserAlreadyDeletedException();
    exception.addDetail("userId", id);
    return exception;
  }
}
