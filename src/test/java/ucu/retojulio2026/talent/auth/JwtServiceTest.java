package ucu.retojulio2026.talent.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import ucu.retojulio2026.talent.user.Role;
import ucu.retojulio2026.talent.user.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private static final long EXPIRATION_MINUTES = 60L;

    @Mock
    private JwtEncoder encoder;

    @Test
    void issue_genera_un_token_con_subject_role_y_expiracion_segun_rf_aut_04() {
        JwtService service = new JwtService(encoder, EXPIRATION_MINUTES);
        User user = new User();
        user.setUserId("user-1");
        user.setRole(Role.ALUMNO);
        Jwt encoded = mock(Jwt.class);
        when(encoded.getTokenValue()).thenReturn("token-value");
        when(encoder.encode(any())).thenReturn(encoded);

        Instant before = Instant.now();
        String token = service.issue(user);
        Instant after = Instant.now();

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(encoder).encode(captor.capture());
        JwtClaimsSet claims = captor.getValue().getClaims();

        assertThat(token).isEqualTo("token-value");
        assertThat(claims.getSubject()).isEqualTo("user-1");
        assertThat(claims.getClaimAsString("role")).isEqualTo("ALUMNO");
        assertThat(claims.getExpiresAt())
                .isBetween(before.plus(EXPIRATION_MINUTES, ChronoUnit.MINUTES),
                        after.plus(EXPIRATION_MINUTES, ChronoUnit.MINUTES));
    }
}
