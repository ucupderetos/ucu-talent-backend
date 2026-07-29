package ucu.retojulio2026.talent.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentTypeTest {

    @ParameterizedTest
    @EnumSource(value = DocumentType.class, names = {"CEDULA_IDENTIDAD", "DNI"})
    void cedula_y_dni_solo_aceptan_digitos(DocumentType documentType) {
        assertThat(documentType.isValid("12345678")).isTrue();
        assertThat(documentType.isValid("12A45678")).isFalse();
    }

    @Test
    void pasaporte_acepta_alfanumerico() {
        assertThat(DocumentType.PASAPORTE.isValid("AB123456")).isTrue();
    }

    @ParameterizedTest
    @EnumSource(DocumentType.class)
    void numero_nulo_da_false_para_cualquier_tipo(DocumentType documentType) {
        assertThat(documentType.isValid(null)).isFalse();
    }
}
