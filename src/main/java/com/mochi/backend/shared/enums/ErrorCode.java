package com.mochi.backend.shared.enums;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INTERNAL_SERVER("Internal server error!", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED("Unauthorized", HttpStatus.UNAUTHORIZED),
    VALIDATION_FAILED("Validation failed!", HttpStatus.UNPROCESSABLE_ENTITY),
    ROLE_NOT_FOUND("Role does not exists!", HttpStatus.NOT_FOUND),
    LOGIN_FAIL("The account or password is incorrect!", HttpStatus.UNAUTHORIZED),
    USER_DISABLE("The account not verified!", HttpStatus.FORBIDDEN),
    ACCOUNT_REGISTERED("The account has been registered!", HttpStatus.BAD_REQUEST),
    SEND_EMAIL_FAIL("Failed to send verification email!", HttpStatus.BAD_REQUEST),
    USER_IS_ENABLED("The account has been verified!", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_REGISTERED("The email not registered!", HttpStatus.BAD_REQUEST),
    VERIFICATION_CODE_EXPIRED("Verification code expired!", HttpStatus.BAD_REQUEST),
    VERIFICATION_CODE_INVALID("Verification code does not match!", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED("Token expired!", HttpStatus.BAD_REQUEST),
    NOT_FOUND_IN_COOKIES("Not found in cookies", HttpStatus.NOT_FOUND),
    REFRESH_TOKEN_INVALID("Refresh token invalid", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_EXISTED("Account not existed!", HttpStatus.BAD_REQUEST),
    TOKEN_EXISTS("Token exists!", HttpStatus.BAD_REQUEST),
    ;
    private final String message;
    
    @JsonIgnore
    private final HttpStatus httpStatus;
}
