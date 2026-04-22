package com.sb09.deokhugam.domain.book.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record BookCreateRequest(

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 500, message = "제목은 500자 이하여야 합니다.")
    String title,

    @NotBlank(message = "저자는 필수입니다.")
    @Size(max = 255, message = "저자는 255자 이하여야 합니다.")
    String author,

    @NotBlank(message = "설명은 필수입니다.")
    @Size(max = 1000, message = "설명은 1000자 이하여야 합니다.")
    String description,

    @NotBlank(message = "출판사는 필수입니다.")
    @Size(max = 255, message = "출판사는 255자 이하여야 합니다.")
    String publisher,

    @NotNull(message = "출판일은 필수입니다.")
    @PastOrPresent(message = "출판일은 현재 또는 과거 날짜여야 합니다.")
    LocalDate publishedDate,

    @NotBlank(message = "ISBN은 필수입니다.")
    @Size(max = 20, message = "ISBN은 20자 이하여야 합니다.")
    String isbn

) {

}
