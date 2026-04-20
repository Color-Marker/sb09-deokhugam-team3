package com.sb09.deokhugam.domain.notification.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDto(
    UUID id,
    UUID userId,
    UUID reviewId,
    String reviewContent,
    String message,
    Boolean confirmed,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) { }
