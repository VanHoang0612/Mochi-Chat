package com.mochi.backend.dto.user;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {
    UUID id;
    String username;
    String email;
    String avatarUrl;
    String firstname;
    String lastname;
}
