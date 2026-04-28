package com.sb09.deokhugam.domain.dashboard.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PopularReviewTop10Event {
  private final UUID reviewId;
  private final Long ranking;
}