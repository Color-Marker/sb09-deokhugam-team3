package com.sb09.deokhugam.domain.book.dto;


import java.time.LocalDate;

public record NaverBookDto(
    String title,
    String author,
    String description,
    String publisher,
    LocalDate publishedDate,
    String isbn,
    String thumbnailImage
) {

}
