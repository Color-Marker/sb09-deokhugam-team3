package com.sb09.deokhugam.global.common.mapper;

import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CursorPageResponseMapper {

  public <T, R> CursorPageResponseDto<R> fromSlice(
      Slice<T> slice,
      Function<T, R> converter,
      Function<T, Object> cursorExtractor,
      Function<T, LocalDateTime> afterExtractor,
      Long totalElements
  ) {
    List<R> dtos = slice.getContent().stream()
        .map(converter)
        .toList();
    Object nextCursor = null;
    LocalDateTime nextAfter = null;
    if (slice.hasNext() && !slice.getContent().isEmpty()) {
      T last = slice.getContent().get(slice.getContent().size() - 1);
      nextCursor = cursorExtractor.apply(last);
      nextAfter = afterExtractor.apply(last);
    }
    return new CursorPageResponseDto<>(
        dtos,
        nextCursor,
        nextAfter,
        dtos.size(),
        totalElements,
        slice.hasNext()
    );
  }
}