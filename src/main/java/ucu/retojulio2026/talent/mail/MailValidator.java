package ucu.retojulio2026.talent.mail;

import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import org.springframework.stereotype.Component;

@Component
public class MailValidator {

    private final Validator validator;

    public MailValidator(Validator validator) {
        this.validator = validator;
    }

    public String normalize(String email) {
        return email != null ? email.trim().toLowerCase() : null;
    }

    public boolean isValid(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return validator.validateValue(EmailWrapper.class, "email", email).isEmpty();
    }

    public void validateOrThrow(String email) {
        if (!isValid(email)) {
            throw new IllegalArgumentException("Formato de email invalido: " + email);
        }
    }

    private static class EmailWrapper {
        @Email(message = "Email invalido")
        private String email;
    }
}