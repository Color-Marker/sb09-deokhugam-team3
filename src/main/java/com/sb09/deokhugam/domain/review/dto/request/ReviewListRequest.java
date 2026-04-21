package com.sb09.deokhugam.domain.review.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Sort;

public record ReviewListRequest(
    UUID bookId,
    UUID userId,
    String keyword,
    Integer limit,
    UUID cursor,
    LocalDateTime after,
    String orderBy,   // 필드명 정렬 기준
    Sort.Direction direction // 정렬 방향 (DESC/ASC)
) {

}