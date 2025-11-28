package com.mochi.backend.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class RefreshTokenRequest {
    private String refreshToken;
}