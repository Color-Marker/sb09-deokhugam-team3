package com.sb09.deokhugam.domain.dashboard.service;

import com.sb09.deokhugam.domain.dashboard.dto.request.PopularBookListRequest;
import com.sb09.deokhugam.domain.dashboard.dto.response.PopularBookDto;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import java.time.LocalDate;

public interface PopularBookService {
  CursorPageResponseDto<PopularBookDto> list(PopularBookListRequest request);
  void calculatePopularBook(LocalDate baseDate);
}
