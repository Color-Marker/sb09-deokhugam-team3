package com.sb09.deokhugam.domain.book.dto;

import com.sb09.deokhugam.domain.dashboard.entity.PeriodType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PopularBookDto(
    UUID id,
    UUID bookId,
    String title,
    String author,
    String thumbnailUrl,
    PeriodType period,
    LocalDate baseDate,
    Long rank,
    BigDecimal score,
    Long reviewCount,
    BigDecimal rating,
    LocalDateTime createdAt
) {

}
