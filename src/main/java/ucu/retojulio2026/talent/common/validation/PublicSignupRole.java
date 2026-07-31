package ucu.retojulio2026.talent.common.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.RECORD_COMPONENT, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PublicSignupRoleValidator.class)
public @interface PublicSignupRole {

    String message() default "El rol debe ser ALUMNO o EMPRESA";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
