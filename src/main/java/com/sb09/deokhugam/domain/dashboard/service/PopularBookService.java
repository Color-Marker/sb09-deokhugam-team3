package com.sb09.deokhugam.domain.dashboard.service;

import java.time.LocalDate;

public interface PopularBookService {
  long calculatePopularBook(LocalDate baseDate);
}
