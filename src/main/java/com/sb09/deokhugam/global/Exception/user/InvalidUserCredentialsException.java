package com.sb09.deokhugam.global.Exception.user;

import com.sb09.deokhugam.global.Exception.ErrorCode;

public class InvalidUserCredentialsException extends UserException {

  //어떤 ID나 값을 상세 정보로 넣을 필요가 없고 "자격증명 실패" 자체로 충분한 정보라서 메서드는 필요없음
  public InvalidUserCredentialsException() {
    super(ErrorCode.INVALID_USER_CREDENTIALS);
  }
}
