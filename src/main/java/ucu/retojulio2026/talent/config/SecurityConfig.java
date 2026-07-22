package ucu.retojulio2026.talent.config;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatchers;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import tools.jackson.databind.ObjectMapper;

import org.springframework.http.ProblemDetail;

import ucu.retojulio2026.talent.auth.CookieBearerTokenResolver;

@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecretKey jwtSecretKey(@Value("${jwt.secret}") String secret) {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey).build();
    }

    // Mapea el claim "role"=ALUMNO -> authority "ROLE_ALUMNO" (lo que espera hasRole(...)).
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("role");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${cors.allowed-origin}") String allowedOrigin) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigin));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Se ejecuta cuando oauth2ResourceServer rechaza un request sin JWT valido, ANTES de que
    // llegue a cualquier controller (por eso GlobalExceptionHandler nunca lo ve). Sin este bean,
    // Spring Security responde 401 vacio + solo el header WWW-Authenticate (RFC 6750) - correcto
    // para clientes OAuth2 maquina-a-maquina, pero inconsistente con el resto de la API (que
    // siempre responde ProblemDetail JSON). Mantenemos el header (cumple el RFC) y agregamos el
    // mismo body que ya usa GlobalExceptionHandler en todos los demas errores.
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
        return (request, response, authException) -> {
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                    HttpStatus.UNAUTHORIZED, "No autenticado (sin cookie o token invalido/vencido)");
            objectMapper.writeValue(response.getOutputStream(), problem);
        };
    }

    // Paths publicos: login/logout, signup, swagger y health checks.
    private static final RequestMatcher PUBLIC_MATCHER = RequestMatchers.anyOf(
            PathPatternRequestMatcher.pathPattern("/auth/**"),
            PathPatternRequestMatcher.pathPattern("/api-docs/**"),
            PathPatternRequestMatcher.pathPattern("/docs/**"),
            PathPatternRequestMatcher.pathPattern("/swagger-ui/**"),
            PathPatternRequestMatcher.pathPattern("/webjars/**"),
            PathPatternRequestMatcher.pathPattern("/actuator/**"),
            PathPatternRequestMatcher.pathPattern("/error"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/user"),
            // TEMPORAL: alta de ADMIN para pruebas. Ver DevAdminController.
            PathPatternRequestMatcher.pathPattern("/dev/**"),
            // WorkExperience: lectura publica por query param (ej: /work-experience?studentProfileId=...)
            PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/work-experience")
    );

    // Cadena 1: paths publicos. NO tiene oauth2ResourceServer -> el filtro que decodifica el
    // JWT ni corre aca. Es la unica forma real de que una cookie access_token vieja/invalida
    // no rompa el login/signup: si el filtro de JWT se ejecutara igual (como pasaba antes,
    // con todo en una sola cadena + permitAll), una cookie corrupta tira una
    // AuthenticationException ANTES de que se llegue a evaluar que el path era permitAll,
    // porque los filtros de autenticacion corren antes que los de autorizacion.
    @Bean
    @Order(1)
    public SecurityFilterChain publicFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(PUBLIC_MATCHER)
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }


    @Bean
    @Order(2)
    public SecurityFilterChain apiFilterChain(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter,
            AuthenticationEntryPoint authenticationEntryPoint) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Company
                        .requestMatchers(HttpMethod.POST, "/company").hasRole("EMPRESA")
                        // La busqueda pueda ser para todos los autenticados
                        .requestMatchers(HttpMethod.GET, "/vacancy/search").authenticated()
                        // Vacancy
                        .requestMatchers(HttpMethod.POST, "/vacancy").hasRole("EMPRESA")
                        // Admin primero así puede hacer el status y el usuario empresa no.
                        .requestMatchers(HttpMethod.PUT, "/vacancy/status/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/vacancy/**").hasRole("EMPRESA")
                        .requestMatchers(HttpMethod.PATCH, "/vacancy/**").hasRole("EMPRESA")
                        .requestMatchers(HttpMethod.DELETE, "/vacancy/**").hasRole("EMPRESA")
                        // Student-Profile
                        .requestMatchers(HttpMethod.POST, "/student-profile").hasRole("ALUMNO")
                        .requestMatchers(HttpMethod.POST, "/vacancy-application").hasRole("ALUMNO")
                        .requestMatchers(HttpMethod.GET, "/vacancy-application/me").hasRole("ALUMNO")
                        // Admin
                        .requestMatchers(HttpMethod.POST, "/audit/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/admin").hasRole("ADMIN")
                        // Listado de usuarios: expone todos los emails, solo ADMIN.
                        .requestMatchers(HttpMethod.GET, "/user").hasRole("ADMIN")
                        // Aprobar/rechazar cuenta: solo ADMIN.
                        .requestMatchers(HttpMethod.PATCH, "/user/{id}").hasRole("ADMIN")
                        // University Registry: exclusivo de ADMIN, incluidos los GET.
                        .requestMatchers("/university-registry/**").hasRole("ADMIN")
                        // Consultas de admin: totales por estado, solo ADMIN.
                        .requestMatchers(HttpMethod.GET, "/company/status-summary").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/student-profile/status-summary").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/vacancy/status-summary").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/vacancy-application/status-summary").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(new CookieBearerTokenResolver())
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint(authenticationEntryPoint));
        return http.build();
    }
}
