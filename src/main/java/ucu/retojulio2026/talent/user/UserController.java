package ucu.retojulio2026.talent.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import ucu.retojulio2026.talent.common.AuthorizationGuard;
import ucu.retojulio2026.talent.common.InvalidStatusTransitionException;
import ucu.retojulio2026.talent.user.dto.CreateUserRequest;
import ucu.retojulio2026.talent.user.dto.UpdateUserStatusRequest;
import ucu.retojulio2026.talent.user.dto.UserMapper;
import ucu.retojulio2026.talent.user.dto.UserResponse;

import java.util.List;

@RestController
@RequestMapping("/user")
@Tag(name = "Usuarios", description = "Alta, consulta y baja de usuarios")
public class UserController {

    private final UserService userService;
    private final UserRegistrationService userRegistrationService;
    private final UserDeletionService userDeletionService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserRegistrationService userRegistrationService,
            UserDeletionService userDeletionService, UserMapper userMapper) {
        this.userService = userService;
        this.userRegistrationService = userRegistrationService;
        this.userDeletionService = userDeletionService;
        this.userMapper = userMapper;
    }

    // ===== CREATE =====


    @Operation(summary = "Crear un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ver el detalle por campo)"),
    })
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        User created = userRegistrationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(created));
    }

    // ===== READ =====

    @Operation(summary = "Listar usuarios, opcionalmente filtrados por estado y/o rol")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "403", description = "No tiene rol ADMIN"),})
    @GetMapping()
    public ResponseEntity<List<UserResponse>> getAll(
            @Parameter(description = "Filtrar por estado de la cuenta", example = "PENDIENTE")
            @RequestParam(required = false) AccountStatus status,
            @Parameter(description = "Filtrar por rol", example = "EMPRESA")
            @RequestParam(required = false) Role role,
            @Parameter(description = "Numero de pagina (0-indexed)") @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Tamaño de pagina") @RequestParam (required = false, defaultValue = "20") int size){
        List<UserResponse> response = userService.getAll(status, role, PageRequest.of(page, size))
                .stream()
                .map(userMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener un usuario por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "404", description = "No existe un usuario con ese id")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(
            @Parameter(description = "Id del usuario") @PathVariable String id) {
        User user = userService.getById(id);
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @Operation(summary = "Buscar un usuario por email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "400", description = "El email tiene un formato invalido"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "404", description = "No existe un usuario con ese email")
    })
    @GetMapping("/mail")
    public ResponseEntity<UserResponse> getByEmail(
            @Parameter(description = "Email exacto del usuario", example = "nicogon@ucu.edu.uy")
            @RequestParam
            @NotBlank(message = "El email es obligatorio")
            @Email(message = "El email no tiene un formato valido")
            String email) {
        User user = userService.getByEmail(email);
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    // ===== UPDATE =====

    @Operation(summary = "Aprobar o rechazar un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado de cuenta actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ver el detalle por campo)"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "403", description = "No tiene rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "No existe un usuario con ese id"),
            @ApiResponse(responseCode = "409", description = "Transicion de estado invalida")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> updateStatus(
            @Parameter(description = "Id del usuario") @PathVariable String id,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        userService.updateStatus(id, request.status());
        User updated = userService.getById(id);
        return ResponseEntity.ok(userMapper.toResponse(updated));
    }

    // ===== DELETE =====

    @Operation(summary = "Eliminar un usuario por id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado (sin contenido)"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "403", description = "Usuario autenticado no tiene permisos para eliminar esta cuenta."),
            @ApiResponse(responseCode = "404", description = "No existe un usuario con ese id")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Id del usuario") @PathVariable String id) {
        AuthorizationGuard.requireOwnership(jwt, id);
        userDeletionService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
