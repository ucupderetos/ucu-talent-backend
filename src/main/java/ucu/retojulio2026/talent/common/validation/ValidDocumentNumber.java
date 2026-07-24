package ucu.retojulio2026.talent.common.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DocumentNumberValidator.class)
public @interface ValidDocumentNumber {

    String message() default "Numero de documento invalido para el tipo indicado";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
