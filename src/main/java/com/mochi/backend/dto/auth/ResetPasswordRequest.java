package com.mochi.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequest {
    private String resetToken;
    
    @NotBlank(message = "{NOT_BLANK}")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+{}\\[\\]:;<>,.?~\\\\/-]).*$", message = "{PASSWORD_REGEX}")
    @Size(min = 8, max = 128, message = "{PASSWORD_SIZE}")
    private String newPassword;
}
