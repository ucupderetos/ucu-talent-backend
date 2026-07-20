package ucu.retojulio2026.talent.auth;

import java.time.Duration;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ucu.retojulio2026.talent.auth.dto.LoginRequest;
import ucu.retojulio2026.talent.user.User;
import ucu.retojulio2026.talent.user.dto.UserMapper;
import ucu.retojulio2026.talent.user.dto.UserResponse;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticacion", description = "Login de usuarios")
public class AuthController {

    private static final String COOKIE_NAME = "access_token";

    private final AuthService authService;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final long expirationMinutes;

    public AuthController(AuthService authService, UserMapper userMapper, JwtService jwtService,
            @Value("${jwt.expiration-minutes}") long expirationMinutes) {
        this.authService = authService;
        this.userMapper = userMapper;
        this.jwtService = jwtService;
        this.expirationMinutes = expirationMinutes;
    }

    @Operation(summary = "Login con email y contraseña")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credenciales validas"),
            @ApiResponse(responseCode = "401", description = "Email o contraseña incorrectos"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (campos vacios)")
    })
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.login(request);
        String token = jwtService.issue(user);

        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofMinutes(expirationMinutes))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(userMapper.toResponse(user));
    }

    @Operation(summary = "Logout: vence la cookie de sesión")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cookie vencida")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie expiredCookie = ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .build();
    }
}
