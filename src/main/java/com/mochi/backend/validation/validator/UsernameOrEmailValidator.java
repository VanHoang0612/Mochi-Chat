package com.mochi.backend.validation.validator;

import com.mochi.backend.validation.constraints.UsernameOrEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UsernameOrEmailValidator implements ConstraintValidator<UsernameOrEmail, String> {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final String USERNAME_REGEX = "^[a-zA-Z0-9._-]+$";

    private int usernameMin;
    private int usernameMax;
    private int emailMax;

    @Override
    public void initialize(UsernameOrEmail constraintAnnotation) {
        this.usernameMin = constraintAnnotation.usernameMin();
        this.usernameMax = constraintAnnotation.usernameMax();
        this.emailMax = constraintAnnotation.emailMax();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim()
                .isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{NOT_BLANK}")
                    .addConstraintViolation();
            return false;
        }
        boolean isEmail = value.matches(EMAIL_REGEX);
        boolean isUsername = value.matches(USERNAME_REGEX);

        if (!isEmail && !isUsername) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{USERNAME_OR_EMAIL_REGEX}")
                    .addConstraintViolation();
            return false;
        } else if (isUsername && (value.length() < usernameMin || value.length() > usernameMax)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{USERNAME_SIZE}")
                    .addConstraintViolation();
            return false;

        } else if (isEmail && value.length() > emailMax) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{EMAIL_SIZE}")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
