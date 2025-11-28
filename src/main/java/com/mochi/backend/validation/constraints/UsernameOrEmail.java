package com.mochi.backend.validation.constraints;

import com.mochi.backend.validation.validator.UsernameOrEmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {UsernameOrEmailValidator.class})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface UsernameOrEmail {
    String message() default "Username or email address is invalid";

    int usernameMin() default 3;

    int usernameMax() default 30;

    int emailMax() default 254;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
