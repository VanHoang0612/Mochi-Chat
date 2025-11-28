package com.mochi.backend.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mochi.backend.shared.dto.user.UserDto;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    String accessToken;
    String refreshToken;
    long expiresInMS;
    UserDto user;
}
