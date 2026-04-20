package com.sb09.deokhugam.domain.review.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewListRequest(
    UUID bookId,
    UUID userId,
    String keyword,
    Integer limit,
    UUID cursor,
    LocalDateTime after,
    String sortBy,
    Integer rating
) {

  // 기본값 설정 생성자
  public ReviewListRequest {
    if (limit == null) {
      limit = 10;
    }

    // 정렬 기준이 안 넘어오면 무조건 최신순으로 세팅
    if (sortBy == null || sortBy.isBlank()) {
      sortBy = "LATEST";
    }
  }
}