package ucu.retojulio2026.talent.common.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ucu.retojulio2026.talent.user.Role;

import static org.assertj.core.api.Assertions.assertThat;

class PublicSignupRoleValidatorTest {

    private final PublicSignupRoleValidator validator = new PublicSignupRoleValidator();

    @Test
    void rol_admin_no_se_puede_auto_asignar_en_alta_publica() {
        assertThat(validator.isValid(Role.ADMIN, null)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ALUMNO", "EMPRESA"})
    void roles_alumno_y_empresa_son_validos_en_alta_publica(Role role) {
        assertThat(validator.isValid(role, null)).isTrue();
    }

    @Test
    void rol_nulo_es_valido_ya_que_lo_maneja_el_not_null_del_dto() {
        assertThat(validator.isValid(null, null)).isTrue();
    }
}
