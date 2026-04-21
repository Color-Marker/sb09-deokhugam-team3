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

}