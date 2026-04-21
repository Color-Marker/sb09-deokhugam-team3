package com.sb09.deokhugam.domain.book.dto.request;

import java.time.LocalDateTime;

public record BookSearchCondition(
    String keyword,       // 제목/저자 검색어 - null이면 전체 조회
    String orderBy,       // 정렬 기준: "createdAt" - 디폴트 값
    String direction,     // 정렬 방향: "DESC" - 디폴트값 / "ASC"
    Object cursor,        // 커서값 (UUID)
    LocalDateTime after,  // 커서 기준 시각
    int limit             // 페이지 크기 (기본 20)
) {

}