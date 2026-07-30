package ucu.retojulio2026.talent.auth;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

import tools.jackson.databind.ObjectMapper;

class SignupRateLimitFilterTest {

    @Test
    void bloquea_la_IP_por_un_minuto_al_crear_mas_cuentas_de_las_permitidas() throws Exception {
        SignupRateLimitFilter filter = new SignupRateLimitFilter(
                new ObjectMapper(),
                true,
                50,
                60,
                3,
                60,
                1_000,
                10,
                "60,300,1800",
                "900,3600,86400",
                43_200);

        MockHttpServletResponse firstResponse = runSignupRequest(filter, "198.51.100.20", "user1@ucu.edu.uy");
        MockHttpServletResponse secondResponse = runSignupRequest(filter, "198.51.100.20", "user2@ucu.edu.uy");
        MockHttpServletResponse thirdResponse = runSignupRequest(filter, "198.51.100.20", "user3@ucu.edu.uy");
        MockHttpServletResponse fourthResponse = runSignupRequest(filter, "198.51.100.20", "user4@ucu.edu.uy");

        assertThat(firstResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(secondResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(thirdResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(fourthResponse.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(fourthResponse.getHeader("Retry-After")).isEqualTo("60"); // 1 minuto, no un dia
        assertThat(fourthResponse.getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        assertThat(fourthResponse.getContentAsString()).contains("Demasiadas cuentas creadas");
    }

    @Test
    void el_body_expone_retryAfterSeconds_con_el_mismo_valor_que_el_header() throws Exception {
        SignupRateLimitFilter filter = new SignupRateLimitFilter(
                new ObjectMapper(),
                true,
                50,
                60,
                3,
                60,
                1_000,
                10,
                "60,300,1800",
                "900,3600,86400",
                43_200);

        runSignupRequest(filter, "198.51.100.30", "user1@ucu.edu.uy");
        runSignupRequest(filter, "198.51.100.30", "user2@ucu.edu.uy");
        runSignupRequest(filter, "198.51.100.30", "user3@ucu.edu.uy");
        MockHttpServletResponse bloqueada = runSignupRequest(filter, "198.51.100.30", "user4@ucu.edu.uy");

        assertThat(bloqueada.getContentAsString()).contains("\"retryAfterSeconds\":60");
        assertThat(bloqueada.getHeader("Retry-After")).isEqualTo("60");
        assertThat(bloqueada.getContentAsString()).doesNotContain("unos segundos");
    }

    @Test
    void la_IP_sigue_bloqueada_mientras_dura_el_castigo() throws Exception {
        SignupRateLimitFilter filter = new SignupRateLimitFilter(
                new ObjectMapper(),
                true,
                50,
                60,
                3,
                60,
                1_000,
                10,
                "60,300,1800",
                "900,3600,86400",
                43_200);

        // 1er strike: agota el limite por IP (bloqueo corto).
        runSignupRequest(filter, "198.51.100.21", "day1-a@ucu.edu.uy");
        runSignupRequest(filter, "198.51.100.21", "day1-b@ucu.edu.uy");
        runSignupRequest(filter, "198.51.100.21", "day1-c@ucu.edu.uy");
        MockHttpServletResponse firstLockout = runSignupRequest(filter, "198.51.100.21", "day1-d@ucu.edu.uy");
        assertThat(firstLockout.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(firstLockout.getHeader("Retry-After")).isEqualTo("60");

        // Sin esperar el minuto real, confirmamos que sigue bloqueado.
        MockHttpServletResponse stillLocked = runSignupRequest(filter, "198.51.100.21", "day1-e@ucu.edu.uy");
        assertThat(stillLocked.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(stillLocked.getHeader("Retry-After")).isEqualTo("60");
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
                "60,300,1800",
                "900,3600,86400",
                1_440);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/user");
        request.setServletPath("/user");
        request.setRemoteAddr("192.0.2.15");

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getHeader("Retry-After")).isNull();
    }

    @Test
    void el_mismo_email_repetido_se_castiga_mucho_mas_que_la_misma_IP() throws Exception {
        SignupRateLimitFilter porEmail = new SignupRateLimitFilter(
                new ObjectMapper(),
                true,
                3,
                60,
                50,
                60,
                1_000,
                10,
                "60,300,1800",
                "900,3600,86400",
                43_200);

        runSignupRequest(porEmail, "198.51.100.40", "repetido@ucu.edu.uy");
        runSignupRequest(porEmail, "198.51.100.41", "repetido@ucu.edu.uy");
        runSignupRequest(porEmail, "198.51.100.42", "repetido@ucu.edu.uy");
        MockHttpServletResponse bloqueadaPorEmail =
                runSignupRequest(porEmail, "198.51.100.43", "repetido@ucu.edu.uy");

        assertThat(bloqueadaPorEmail.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(bloqueadaPorEmail.getHeader("Retry-After")).isEqualTo("900"); // 15 minutos

        SignupRateLimitFilter porIp = new SignupRateLimitFilter(
                new ObjectMapper(),
                true,
                50,
                60,
                3,
                60,
                1_000,
                10,
                "60,300,1800",
                "900,3600,86400",
                43_200);

        runSignupRequest(porIp, "198.51.100.50", "a@ucu.edu.uy");
        runSignupRequest(porIp, "198.51.100.50", "b@ucu.edu.uy");
        runSignupRequest(porIp, "198.51.100.50", "c@ucu.edu.uy");
        MockHttpServletResponse bloqueadaPorIp = runSignupRequest(porIp, "198.51.100.50", "d@ucu.edu.uy");

        assertThat(bloqueadaPorIp.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(bloqueadaPorIp.getHeader("Retry-After")).isEqualTo("60"); // 1 minuto
    }

    @Test
    void loguea_cada_bloqueo_con_la_clave_que_lo_disparo_y_el_email_enmascarado() throws Exception {
        ch.qos.logback.classic.Logger logger =
                ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(SignupRateLimitFilter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            SignupRateLimitFilter filter = new SignupRateLimitFilter(
                    new ObjectMapper(),
                    true,
                    50,
                    60,
                    1,
                    60,
                    1_000,
                    10,
                    "60,300,1800",
                    "900,3600,86400",
                    43_200);

            runSignupRequest(filter, "198.51.100.60", "ana.perez@correo.ucu.edu.uy");
            runSignupRequest(filter, "198.51.100.60", "bruno.diaz@correo.ucu.edu.uy");

            String linea = appender.list.stream()
                    .filter(evento -> evento.getLevel() == Level.WARN)
                    .map(ILoggingEvent::getFormattedMessage)
                    .findFirst()
                    .orElseThrow();

            assertThat(linea).contains("rate_limit_blocked");
            assertThat(linea).contains("endpoint=\"POST /user\"");
            assertThat(linea).contains("clave=ip");
            assertThat(linea).contains("ip=198.51.100.60");
            assertThat(linea).contains("retryAfterSeconds=60");
            assertThat(linea).contains("email=b***@correo.ucu.edu.uy");
            assertThat(linea).doesNotContain("bruno.diaz@");
        } finally {
            logger.detachAppender(appender);
        }
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
