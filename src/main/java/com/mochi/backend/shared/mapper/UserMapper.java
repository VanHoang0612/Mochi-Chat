package com.mochi.backend.shared.mapper;

import com.mochi.backend.shared.dto.user.UserDto;
import com.mochi.backend.user.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
}
