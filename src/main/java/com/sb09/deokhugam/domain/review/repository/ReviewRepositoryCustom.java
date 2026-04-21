package com.sb09.deokhugam.domain.review.repository;

import com.sb09.deokhugam.domain.review.dto.request.ReviewListRequest;
import com.sb09.deokhugam.domain.review.dto.response.ReviewDto; // 🌟 DTO import 추가
import org.springframework.data.domain.Slice;

import java.util.UUID;

public interface ReviewRepositoryCustom {

  Slice<ReviewDto> searchReviews(ReviewListRequest request, UUID requestUserId);

}