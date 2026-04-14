package com.sb09.deokhugam.domain.comment.dto.request;

import java.util.UUID;

public record CommentCreateRequest(
    UUID reviewId,
    UUID userId,
    String content
) {

}
