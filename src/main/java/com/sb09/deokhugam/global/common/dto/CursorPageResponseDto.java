package com.sb09.deokhugam.global.common.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CursorPageResponseDto<T>(
    List<T> content,
    Object nextCursor,
    LocalDateTime nextAfter,
    int size,
    Long totalElements,
    boolean hasNext
) {

}
