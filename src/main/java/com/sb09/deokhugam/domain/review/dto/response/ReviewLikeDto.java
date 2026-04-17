package com.sb09.deokhugam.domain.review.dto.response;

public record ReviewLikeDto(
    boolean liked,   // 현재 내가 좋아요 한 상태인지
    int likeCount    // 반영된 총 좋아요 개수
) {

}
