package com.mochi.backend.dto.user;

import com.mochi.backend.validation.constraints.PasswordMatches;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@PasswordMatches
public class ChangePasswordRequest {

    @NotBlank(message = "{NOT_BLANK}")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+{}\\[\\]:;<>,.?~\\\\/-]).*$", message = "{PASSWORD_REGEX}")
    @Size(min = 8, max = 128, message = "{PASSWORD_SIZE}")
    String oldPassword;

    @NotBlank(message = "{NOT_BLANK}")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+{}\\[\\]:;<>,.?~\\\\/-]).*$", message = "{PASSWORD_REGEX}")
    @Size(min = 8, max = 128, message = "{PASSWORD_SIZE}")
    String newPassword;

    @NotBlank(message = "{NOT_BLANK}")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+{}\\[\\]:;<>,.?~\\\\/-]).*$", message = "{PASSWORD_REGEX}")
    @Size(min = 8, max = 128, message = "{PASSWORD_SIZE}")
    String confirmNewPassword;
}
