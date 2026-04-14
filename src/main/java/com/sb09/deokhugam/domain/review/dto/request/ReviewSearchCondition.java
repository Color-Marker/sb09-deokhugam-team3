package com.sb09.deokhugam.domain.review.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewSearchCondition(
    String keyword,
    UUID userId,
    UUID bookId,
    Integer cursorRating,
    LocalDateTime after
) {

}
