package com.sb09.deokhugam.domain.review.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewDto(
    UUID id,
    UUID bookId,
    UUID userId,
    String content,
    Integer rating,
    Integer likeCount,
    Integer commentCount,
    Boolean likedByMe,
    LocalDateTime createdAt,
    LocalDateTime updatedAt

) {

}
