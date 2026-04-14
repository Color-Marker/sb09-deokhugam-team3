package com.sb09.deokhugam.domain.review.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewPageResponse(
    List<ReviewResponse> content, // 리뷰 목록
    String nextCursor,            // 다음 조회를 위한 커서 값
    LocalDateTime nextAfter,      // 다음 조회를 위한 기준 시간
    int size,                     // 가져온 개수
    long totalElements,           // 전체 리뷰 수
    boolean hasNext               // 다음 페이지 존재 여부
) {

}
