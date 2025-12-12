package com.mochi.backend.exception;

import com.mochi.backend.dto.api.ApiResponse;
import com.mochi.backend.enums.ErrorCode;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(value = {AppException.class})
    ResponseEntity<ApiResponse<Void>> handleAppException(AppException e) {
        ErrorCode errorCode = e.getErrorCode();
        List<String> errors = e.getErrors();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(
                        ApiResponse.failure(errorCode, errors)
                );
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        List<String> fieldOrder = List.of("username", "email", "password", "firstname", "lastname");

        BindingResult bindingResult = e.getBindingResult();

        List<Map<String, Object>> attributesList = bindingResult.getAllErrors()
                .stream()
                .map(err -> {
                    ConstraintViolation<?> violation = err.unwrap(ConstraintViolation.class); // ép kiểu ConstraintViolation, unwrap để có thể truy cập sâu vào lỗi gốc
                    Map<String, Object> map = new HashMap<>(violation.getConstraintDescriptor()
                            .getAttributes()); // tạo map chứa thuộc tính và giá trị của lỗi
                    map.put("field", violation.getPropertyPath()
                            .toString()); // thêm key field với giá trị là trường lỗi
                    return map;
                })
                .toList();
        log.info(attributesList.toString());
        List<String> errors = bindingResult.getFieldErrors()
                .stream()
                .sorted(Comparator.comparingInt(fieldError -> fieldOrder.indexOf(fieldError.getField())))
                .map(fieldError -> mapAttributes(fieldError.getDefaultMessage(), attributesList, fieldError.getField()))
                .toList();
        log.info(errors.toString());
        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.getHttpStatus())
                .body(
                        ApiResponse.<Void>builder()
                                .success(false)
                                .message(ErrorCode.VALIDATION_FAILED.getMessage())
                                .errors(errors)
                                .build()
                );
    }

    private String mapAttributes(String message, List<Map<String, Object>> attributesList, String fieldName) {
        Optional<Map<String, Object>> matchedAttributes = attributesList
                .stream()
                .filter(attr -> fieldName.equals(attr.get("field")))
                .findFirst();

        if (matchedAttributes.isPresent()) {
            Map<String, Object> attr = matchedAttributes.get();
            Object minValue = attr.get("min");
            Object maxValue = attr.get("max");


            if (minValue != null) {
                message = message.replace("{min}", minValue.toString());
            }
            if (maxValue != null) {
                message = message.replace("{max}", maxValue.toString());
            }
            message = message.replace("{field}", fieldName);
        }

        return message;
    }

}
