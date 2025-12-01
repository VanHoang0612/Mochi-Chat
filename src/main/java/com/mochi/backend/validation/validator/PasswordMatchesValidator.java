package com.mochi.backend.validation.validator;

import com.mochi.backend.dto.user.ChangePasswordRequest;
import com.mochi.backend.validation.constraints.PasswordMatches;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, ChangePasswordRequest> {

    @Override
    public boolean isValid(ChangePasswordRequest request, ConstraintValidatorContext context) {
        if (request.getNewPassword() == null || request.getConfirmNewPassword() == null) {
            return false;
        }
        return request.getNewPassword()
                .equals(request.getConfirmNewPassword());

    }
}
