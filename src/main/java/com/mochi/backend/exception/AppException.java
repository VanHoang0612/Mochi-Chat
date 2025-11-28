package com.mochi.backend.exception;

import com.mochi.backend.shared.enums.ErrorCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter

public class AppException extends RuntimeException {
    private final ErrorCode errorCode;
    private final List<String> errors;

    public AppException(ErrorCode errorCode, List<String> errors) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.errors = errors;
    }

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.errors = null;
    }
}
