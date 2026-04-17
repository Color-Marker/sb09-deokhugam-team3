package com.sb09.deokhugam.domain.review.mapper;

import com.sb09.deokhugam.domain.book.entity.Book;
import com.sb09.deokhugam.domain.review.dto.response.ReviewDto;
import com.sb09.deokhugam.domain.review.entity.Review;
import com.sb09.deokhugam.domain.user.entity.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {

  @Mapping(target = "id", source = "review.id")
  @Mapping(target = "bookId", source = "review.bookId")
  @Mapping(target = "userId", source = "review.userId")
  @Mapping(target = "content", source = "review.content")
  @Mapping(target = "rating", source = "review.rating")
  @Mapping(target = "likeCount", source = "review.likeCount")
  @Mapping(target = "commentCount", source = "review.commentCount")
  @Mapping(target = "createdAt", source = "review.createdAt")
  @Mapping(target = "updatedAt", source = "review.updatedAt")
  // 타 도메인 데이터 매핑
  @Mapping(target = "bookTitle", source = "book.title")
  @Mapping(target = "bookThumbnailUrl", source = "book.thumbnailUrl")
  @Mapping(target = "userNickname", source = "user.nickname")
  // 내가 좋아요 눌렀는지 여부
  @Mapping(target = "likedByMe", source = "likedByMe")
  ReviewDto toDto(Review review, Book book, Users user, Boolean likedByMe);
}