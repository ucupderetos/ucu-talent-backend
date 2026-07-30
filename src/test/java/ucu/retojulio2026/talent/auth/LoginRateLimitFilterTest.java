package ucu.retojulio2026.talent.auth;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

import tools.jackson.databind.ObjectMapper;

class LoginRateLimitFilterTest {

    @Test
    void bloquea_cuando_supera_limite_por_email() throws Exception {
        LoginRateLimitFilter filter = new LoginRateLimitFilter(
                new ObjectMapper(),
                true,
                2,
                60,
                20,
                60,
                1_000,
                10,
                "60,300,1800",
                "30,180,900",
                1_440);

        MockHttpServletResponse firstResponse = runLoginRequest(filter, "198.51.100.10", "mail@ucu.edu.uy");
        MockHttpServletResponse secondResponse = runLoginRequest(filter, "198.51.100.10", "mail@ucu.edu.uy");
        MockHttpServletResponse thirdResponse = runLoginRequest(filter, "198.51.100.10", "mail@ucu.edu.uy");

        assertThat(firstResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(secondResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(thirdResponse.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(thirdResponse.getHeader("Retry-After")).isEqualTo("30");
        assertThat(thirdResponse.getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        assertThat(thirdResponse.getContentAsString()).contains("Demasiados intentos de login");
    }

    @Test
    void bloquea_cuando_supera_limite_por_ip_aunque_cambie_email() throws Exception {
        LoginRateLimitFilter filter = new LoginRateLimitFilter(
                new ObjectMapper(),
                true,
                50,
                60,
                2,
                60,
                1_000,
                10,
                "60,300,1800",
                "30,180,900",
                1_440);

        MockHttpServletResponse firstResponse = runLoginRequest(filter, "203.0.113.77", "a@ucu.edu.uy");
        MockHttpServletResponse secondResponse = runLoginRequest(filter, "203.0.113.77", "b@ucu.edu.uy");
        MockHttpServletResponse thirdResponse = runLoginRequest(filter, "203.0.113.77", "c@ucu.edu.uy");

        assertThat(firstResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(secondResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(thirdResponse.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        // La IP usa su propia escala, mas corta que la del email.
        assertThat(thirdResponse.getHeader("Retry-After")).isEqualTo("60");
    }

    @Test
    void el_bloqueo_escala_en_cada_reincidencia() throws Exception {
        LoginRateLimitFilter filter = new LoginRateLimitFilter(
                new ObjectMapper(),
                true,
                1,
                60,
                20,
                60,
                1_000,
                10,
                "60,300,1800",
                "30,180,900",
                1_440);

        // 1ra infraccion: request 1 pasa (consume el unico token), request 2 excede -> 30s.
        runLoginRequest(filter, "192.0.2.50", "reincidente@ucu.edu.uy");
        MockHttpServletResponse firstLockout = runLoginRequest(filter, "192.0.2.50", "reincidente@ucu.edu.uy");
        assertThat(firstLockout.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(firstLockout.getHeader("Retry-After")).isEqualTo("30");

        // Mientras sigue bloqueado, cualquier otro intento repite el mismo castigo (no escala solo).
        MockHttpServletResponse stillLocked = runLoginRequest(filter, "192.0.2.50", "reincidente@ucu.edu.uy");
        assertThat(stillLocked.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(stillLocked.getHeader("Retry-After")).isEqualTo("30");
    }

    @Test
    void no_filtra_en_rutas_que_no_son_login() throws Exception {
        LoginRateLimitFilter filter = new LoginRateLimitFilter(
                new ObjectMapper(),
                true,
                1,
                60,
                1,
                60,
                1_000,
                10,
                "60,300,1800",
                "30,180,900",
                1_440);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/logout");
        request.setServletPath("/auth/logout");
        request.setRemoteAddr("192.0.2.9");

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getHeader("Retry-After")).isNull();
    }

    private MockHttpServletResponse runLoginRequest(LoginRateLimitFilter filter, String ip, String email)
            throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/login");
        request.setServletPath("/auth/login");
        request.setRemoteAddr(ip);
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent(("{\"email\":\"" + email + "\",\"password\":\"secret\"}").getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
