package com.photoconnect.auth.mapper;

import com.photoconnect.auth.domain.User;
import com.photoconnect.auth.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserDto toDto(User user);
}
