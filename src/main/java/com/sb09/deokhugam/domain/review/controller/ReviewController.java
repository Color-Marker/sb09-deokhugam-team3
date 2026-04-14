package com.sb09.deokhugam.domain.review.controller;

import com.sb09.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sb09.deokhugam.domain.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;

  @PostMapping
  public ResponseEntity<Void> createReview(
      @RequestHeader("X-User-Id") UUID userId,
      @Valid @RequestBody ReviewCreateRequest request) {

    reviewService.createReview(request, userId);

    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}