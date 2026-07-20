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
import ucu.retojulio2026.talent.company.Company;
import ucu.retojulio2026.talent.company.CompanyService;
import ucu.retojulio2026.talent.user.Role;
import ucu.retojulio2026.talent.user.User;
import ucu.retojulio2026.talent.user.UserService;

@RestController
@RequestMapping("/me")
@Tag(name = "Autenticacion", description = "Login de usuarios")
public class MeController {

    private final UserService userService;
    private final CompanyService companyService;

    public MeController(UserService userService, CompanyService companyService) {
        this.userService = userService;
        this.companyService = companyService;
    }

    @Operation(summary = "Datos del usuario logueado (hidrata la sesion en el front)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario autenticado"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)")
    })
    @GetMapping
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        User user = userService.getById(userId);

        Boolean approved = null;
        if (user.getRole() == Role.EMPRESA) {
            Company company = companyService.getByUserId(userId);
            approved = company.getApproved();
        }

        MeResponse response = new MeResponse(
                user.getUserId(),
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getRole(),
                user.getPhoneNumber(),
                user.getDocumentType(),
                user.getDocumentNumber(),
                user.getLinkedinUrl(),
                user.getRegisteredAt(),
                approved
        );

        return ResponseEntity.ok(response);
    }
}
