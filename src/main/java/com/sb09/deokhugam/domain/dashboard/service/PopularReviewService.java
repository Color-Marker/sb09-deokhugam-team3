package com.sb09.deokhugam.domain.dashboard.service;

import java.time.LocalDate;

public interface PopularReviewService {
  long calculatePopularReview(LocalDate baseDate);
}