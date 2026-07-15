package ucu.retojulio2026.talent.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ucu.retojulio2026.talent.user.Role;

public class PublicSignupRoleValidator implements ConstraintValidator<PublicSignupRole, Role> {

    @Override
    public boolean isValid(Role value, ConstraintValidatorContext context) {
        return value == null || value != Role.ADMIN;
    }
}
