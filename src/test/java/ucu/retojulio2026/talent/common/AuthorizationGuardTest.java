package ucu.retojulio2026.talent.common;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthorizationGuardTest {

    @Test
    void mismo_subject_que_el_owner_no_lanza_excepcion() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("owner-1");

        assertThatCode(() -> AuthorizationGuard.requireOwnership(jwt, "owner-1"))
                .doesNotThrowAnyException();
    }

    @Test
    void distinto_subject_lanza_forbidden_operation_exception() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("owner-1");

        assertThrows(ForbiddenOperationException.class,
                () -> AuthorizationGuard.requireOwnership(jwt, "owner-2"));
    }
}
