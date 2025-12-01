package com.mochi.backend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddUserRequest {
    @NotBlank(message = "{NOT_BLANK}")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "{USERNAME_REGEX}")
    @Size(min = 3, max = 30, message = "{USERNAME_SIZE}")
    String username;

    @NotBlank(message = "{NOT_BLANK}")
    @Email(message = "{EMAIL_REGEX}")
    @Size(max = 254, message = "{EMAIL_SIZE}")
    String email;

    @NotBlank(message = "{NOT_BLANK}")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+{}\\[\\]:;<>,.?~\\\\/-]).*$", message = "{PASSWORD_REGEX}")
    @Size(min = 8, max = 128, message = "{PASSWORD_SIZE}")
    String password;

    @NotBlank(message = "{NOT_BLANK}")
    @Pattern(regexp = "^[\\p{L} ]+$", message = "{FIRSTNAME_REGEX}")
    @Size(min = 2, max = 50, message = "{FIRSTNAME_SIZE}")
    String firstname;

    @NotBlank(message = "{NOT_BLANK}")
    @Pattern(regexp = "^[\\p{L} ]+$", message = "{LASTNAME_REGEX}")
    @Size(min = 2, max = 50, message = "{LASTNAME_SIZE}")
    String lastname;

    List<String> roles;

    boolean enabled;

}
