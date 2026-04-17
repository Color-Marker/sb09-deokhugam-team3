package com.sb09.deokhugam.domain.review.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewListRequest(
    String keyword,
    Integer limit,
    UUID cursor,
    LocalDateTime after
) {

  // 기본값 설정 생성자
  public ReviewListRequest {
    if (limit == null) {
      limit = 10;
    }
  }
}