package com.mochi.backend.dto.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mochi.backend.enums.ErrorCode;
import com.mochi.backend.enums.SuccessCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String code;
    private String message;
    List<String> errors;
    T data;


    public static <T> ApiResponse<T> success(T data, SuccessCode successCode) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(successCode.getMessage())
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> failure(ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(errorCode.getMessage())
                .build();
    }

}
