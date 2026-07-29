package ucu.retojulio2026.talent.auth;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

import tools.jackson.databind.ObjectMapper;

class SignupRateLimitFilterTest {

    @Test
    void bloquea_por_un_dia_al_crear_mas_de_3_cuentas_distintas_en_un_minuto() throws Exception {
        SignupRateLimitFilter filter = new SignupRateLimitFilter(
                new ObjectMapper(),
                true,
                50,
                60,
                3,
                60,
                1_000,
                10,
                "86400,259200,604800",
                43_200);

        MockHttpServletResponse firstResponse = runSignupRequest(filter, "198.51.100.20", "user1@ucu.edu.uy");
        MockHttpServletResponse secondResponse = runSignupRequest(filter, "198.51.100.20", "user2@ucu.edu.uy");
        MockHttpServletResponse thirdResponse = runSignupRequest(filter, "198.51.100.20", "user3@ucu.edu.uy");
        MockHttpServletResponse fourthResponse = runSignupRequest(filter, "198.51.100.20", "user4@ucu.edu.uy");

        assertThat(firstResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(secondResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(thirdResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(fourthResponse.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(fourthResponse.getHeader("Retry-After")).isEqualTo("86400"); // 1 dia
        assertThat(fourthResponse.getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        assertThat(fourthResponse.getContentAsString()).contains("Demasiadas cuentas creadas");
    }

    @Test
    void escala_a_3_dias_si_reincide_al_dia_siguiente() throws Exception {
        SignupRateLimitFilter filter = new SignupRateLimitFilter(
                new ObjectMapper(),
                true,
                50,
                60,
                3,
                60,
                1_000,
                10,
                "86400,259200,604800",
                43_200);

        // 1er strike: agota el limite (bloqueo de 1 dia).
        runSignupRequest(filter, "198.51.100.21", "day1-a@ucu.edu.uy");
        runSignupRequest(filter, "198.51.100.21", "day1-b@ucu.edu.uy");
        runSignupRequest(filter, "198.51.100.21", "day1-c@ucu.edu.uy");
        MockHttpServletResponse firstLockout = runSignupRequest(filter, "198.51.100.21", "day1-d@ucu.edu.uy");
        assertThat(firstLockout.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(firstLockout.getHeader("Retry-After")).isEqualTo("86400");

        // Nada mas correr esto (sin poder esperar 1 dia real), confirmamos que sigue bloqueado.
        MockHttpServletResponse stillLocked = runSignupRequest(filter, "198.51.100.21", "day1-e@ucu.edu.uy");
        assertThat(stillLocked.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(stillLocked.getHeader("Retry-After")).isEqualTo("86400");
    }

    @Test
    void no_filtra_rutas_que_no_son_alta_de_cuenta() throws Exception {
        SignupRateLimitFilter filter = new SignupRateLimitFilter(
                new ObjectMapper(),
                true,
                1,
                60,
                1,
                60,
                1_000,
                10,
                "30,180,900",
                1_440);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/user");
        request.setServletPath("/user");
        request.setRemoteAddr("192.0.2.15");

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getHeader("Retry-After")).isNull();
    }

    private MockHttpServletResponse runSignupRequest(SignupRateLimitFilter filter, String ip, String email)
            throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/user");
        request.setServletPath("/user");
        request.setRemoteAddr(ip);
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent(
                ("{\"email\":\"" + email + "\",\"password\":\"unaClaveSegura123\",\"role\":\"ALUMNO\"}").getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
