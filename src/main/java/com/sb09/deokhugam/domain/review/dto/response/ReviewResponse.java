package com.sb09.deokhugam.domain.review.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponse(
    UUID id,
    UUID bookId,
    String bookTitle,
    String bookThumbnailUrl, // 썸네일
    UUID userId,
    String userNicknama,
    String content,
    Integer rating,
    Integer likeCount,
    Integer commentCount,
    Boolean likedByMe,
    LocalDateTime createdAt,
    LocalDateTime updatedAt

) {

}
