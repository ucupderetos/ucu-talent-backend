package ucu.retojulio2026.talent.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentNormalizerTest {

    @Test
    void normalize_saca_puntos_guiones_y_espacios() {
        assertThat(DocumentNormalizer.normalize("1.234.567-8")).isEqualTo("12345678");
        assertThat(DocumentNormalizer.normalize("1 234 567 8")).isEqualTo("12345678");
    }

    @Test
    void normalize_de_null_devuelve_null() {
        assertThat(DocumentNormalizer.normalize(null)).isNull();
    }
}
