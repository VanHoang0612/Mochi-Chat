package com.mochi.backend.security.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReSendVerificationCodeRequest {
    @NotBlank(message = "{NOT_BLANK}")
    @Email(message = "{EMAIL_REGEX}")
    @Size(max = 254, message = "{EMAIL_SIZE}")
    private String email;
}
