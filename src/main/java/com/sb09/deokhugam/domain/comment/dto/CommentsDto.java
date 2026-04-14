package com.sb09.deokhugam.domain.comment.dto;

import java.util.UUID;

public record CommentsDto(
    UUID id,
    UUID reviewId,
    UUID userId,
    String userNickname,
    String content
) {

}
