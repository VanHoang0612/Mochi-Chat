package com.mochi.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessCode {
    SUCCESS("Success", HttpStatus.OK),
    ADDUSER("Add user success", HttpStatus.CREATED),
    REGISTER("Register success", HttpStatus.CREATED),
    VERIFY_EMAIL("Verify email success", HttpStatus.OK),
    RESEND_VERIFICATION_CODE("Resend verification code success", HttpStatus.OK),
    LOGIN("Login success", HttpStatus.OK),
    REFRESH_TOKEN("Refresh token success", HttpStatus.OK),
    VERIFY_OTP("Verify otp success", HttpStatus.OK),
    ;

    private final String message;
    private final HttpStatus status;
}
