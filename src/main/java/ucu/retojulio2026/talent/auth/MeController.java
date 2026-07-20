package ucu.retojulio2026.talent.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ucu.retojulio2026.talent.auth.dto.MeResponse;
import ucu.retojulio2026.talent.user.User;
import ucu.retojulio2026.talent.user.UserService;

@RestController
@RequestMapping("/me")
@Tag(name = "Autenticacion", description = "Login de usuarios")
public class MeController {

    private final UserService userService;

    public MeController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Datos del usuario logueado (hidrata la sesion en el front)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario autenticado"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)")
    })
    @GetMapping
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal Jwt jwt) {

        User user = userService.getById(jwt.getSubject());

        MeResponse response = new MeResponse(
                user.getUserId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getRegisteredAt()
        );

        return ResponseEntity.ok(response);
    }
}
