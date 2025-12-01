package com.mochi.backend.mapper;

import com.mochi.backend.dto.user.UserDto;
import com.mochi.backend.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
}
