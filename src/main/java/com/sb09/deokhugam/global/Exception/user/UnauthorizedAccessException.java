package com.sb09.deokhugam.global.Exception.user;

import com.sb09.deokhugam.global.Exception.ErrorCode;

public class UnauthorizedAccessException extends UserException {

  //어떤 ID나 값을 상세 정보로 넣을 필요가 없고 "권한 없음" 자체로 충분한 정보라서 메서드는 필요없음
  public UnauthorizedAccessException() {
    super(ErrorCode.UNAUTHORIZED_ACCESS);
  }

}
