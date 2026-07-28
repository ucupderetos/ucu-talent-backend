package ucu.retojulio2026.talent.common.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ucu.retojulio2026.talent.common.DocumentType;
import ucu.retojulio2026.talent.studentprofile.dto.CreateStudentProfileRequest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentNumberValidatorTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    private CreateStudentProfileRequest requestWith(DocumentType documentType, String documentNumber) {
        return new CreateStudentProfileRequest(
                "Nicolas", "Gonzalez", documentType, documentNumber, null, null, null, null);
    }

    @Test
    void documento_de_cedula_con_puntos_y_guion_se_normaliza_y_es_valido() {
        CreateStudentProfileRequest request = requestWith(DocumentType.CEDULA_IDENTIDAD, "1.234.567-8");

        Set<ConstraintViolation<CreateStudentProfileRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void tipo_de_documento_nulo_es_invalido() {
        CreateStudentProfileRequest request = requestWith(null, "1.234.567-8");

        Set<ConstraintViolation<CreateStudentProfileRequest>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("documentType"));
    }

    @Test
    void numero_de_documento_nulo_es_invalido() {
        CreateStudentProfileRequest request = requestWith(DocumentType.CEDULA_IDENTIDAD, null);

        Set<ConstraintViolation<CreateStudentProfileRequest>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("documentNumber"));
    }
}
