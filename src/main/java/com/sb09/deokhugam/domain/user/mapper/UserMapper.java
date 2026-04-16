package com.sb09.deokhugam.domain.user.mapper;

import com.sb09.deokhugam.domain.user.dto.Response.UserResponse;
import com.sb09.deokhugam.domain.user.entity.Users;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING, // 스프링 빈으로 등록
    unmappedTargetPolicy = ReportingPolicy.IGNORE           // 필드 불일치 시 경고 무시
)
public interface UserMapper {

  UserResponse toDto(Users user);

  List<UserResponse> toDtoList(List<Users> users);

}
