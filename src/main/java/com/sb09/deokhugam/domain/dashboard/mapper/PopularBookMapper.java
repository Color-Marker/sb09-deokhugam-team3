package com.sb09.deokhugam.domain.dashboard.mapper;

import com.sb09.deokhugam.domain.dashboard.dto.response.PopularBookDto;
import com.sb09.deokhugam.domain.dashboard.entity.PopularBook;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PopularBookMapper {
  PopularBookDto toDto(PopularBook popularBook);
}
