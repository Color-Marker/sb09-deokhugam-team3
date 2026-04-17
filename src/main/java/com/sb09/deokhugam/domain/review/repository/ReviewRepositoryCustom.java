package com.sb09.deokhugam.domain.review.repository;

import com.sb09.deokhugam.domain.review.dto.request.ReviewListRequest;
import com.sb09.deokhugam.domain.review.entity.Review;
import org.springframework.data.domain.Slice;

public interface ReviewRepositoryCustom {

  Slice<Review> searchReviews(ReviewListRequest request);

}
