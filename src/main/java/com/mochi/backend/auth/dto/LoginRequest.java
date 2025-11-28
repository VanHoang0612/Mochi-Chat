package com.mochi.backend.auth.dto;

import com.mochi.backend.validation.constraints.UsernameOrEmail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginRequest {
    @UsernameOrEmail
    String usernameOrEmail;

    @NotBlank(message = "{NOT_BLANK}")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+{}\\[\\]:;<>,.?~\\\\/-]).*$", message = "{PASSWORD_REGEX}")
    @Size(min = 8, max = 128, message = "{PASSWORD_SIZE}")
    String password;
}
