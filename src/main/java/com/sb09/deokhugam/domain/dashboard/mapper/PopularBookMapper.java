package com.sb09.deokhugam.domain.dashboard.mapper;

import com.sb09.deokhugam.domain.dashboard.dto.response.PopularBookDto;
import com.sb09.deokhugam.domain.dashboard.entity.PopularBook;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PopularBookMapper {
  @Mapping(target = "bookId", source = "book.id")
  @Mapping(target = "rank", source = "ranking")
  @Mapping(target = "title", source = "book.title")
  @Mapping(target = "author", source = "book.author")
  @Mapping(target = "thumbnailUrl", source = "book.thumbnailUrl")
  PopularBookDto toDto(PopularBook popularBook);
}
