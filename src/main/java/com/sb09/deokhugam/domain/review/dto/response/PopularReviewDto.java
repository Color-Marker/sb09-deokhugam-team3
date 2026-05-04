package com.sb09.deokhugam.domain.review.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record PopularReviewDto(
    UUID id,
    UUID bookId,
    String bookTitle,
    String bookThumbnailUrl,
    UUID userId,
    String userNickname,
    String content,
    Integer rating,
    Long likeCount,
    Long commentCount,
    LocalDateTime createdAt
) {

}