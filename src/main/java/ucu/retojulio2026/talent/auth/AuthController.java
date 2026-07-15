package ucu.retojulio2026.talent.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    private final AuthService authService;
    private final UserMapper userMapper;

    public AuthController(AuthService authService, UserMapper userMapper) {
        this.authService = authService;
        this.userMapper = userMapper;
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
        return ResponseEntity.ok(userMapper.toResponse(user));
    }
}
