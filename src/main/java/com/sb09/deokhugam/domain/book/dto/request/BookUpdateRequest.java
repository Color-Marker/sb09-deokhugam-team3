package com.sb09.deokhugam.domain.book.dto.request;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record BookUpdateRequest(

    // PATCH이므로 null = 수정 안 함. 값이 있다면 빈 문자열은 거부 (min = 1)
    @Size(min = 1, max = 500, message = "제목은 1자 이상 500자 이하여야 합니다.")
    String title,

    @Size(min = 1, max = 255, message = "저자는 1자 이상 255자 이하여야 합니다.")
    String author,

    @Size(min = 1, message = "설명은 1자 이상이어야 합니다.")
    String description,

    @Size(min = 1, max = 255, message = "출판사는 1자 이상 255자 이하여야 합니다.")
    String publisher,

    @PastOrPresent(message = "출판일은 현재 또는 과거 날짜여야 합니다.")
    LocalDate publishedDate

) {
}