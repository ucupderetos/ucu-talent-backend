package ucu.retojulio2026.talent.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import ucu.retojulio2026.talent.admin.dto.AdminMapper;
import ucu.retojulio2026.talent.admin.dto.AdminResponse;
import ucu.retojulio2026.talent.admin.dto.CreateAdminRequest;
import ucu.retojulio2026.talent.admin.dto.UpdateAdminRequest;
import ucu.retojulio2026.talent.common.AuthorizationGuard;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admins", description = "Alta, consulta y baja de admins")
public class AdminController {

    private final AdminService adminService;
    private final AdminMapper adminMapper;

    public AdminController(AdminService adminService, AdminMapper adminMapper) {
        this.adminService = adminService;
        this.adminMapper = adminMapper;
    }

    // ===== CREATE =====

    @Operation(summary = "Crear el admin del usuario logueado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Admin creado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ver el detalle por campo)"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "403", description = "No tiene rol ADMIN"),
            @ApiResponse(responseCode = "409", description = "El usuario ya tiene un admin asociado")
    })
    @PostMapping
    public ResponseEntity<AdminResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateAdminRequest request) {
        Admin created = adminService.create(jwt.getSubject(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(adminMapper.toResponse(created));
    }

    // ===== READ =====

    @Operation(summary = "Listar todos los admins (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "403", description = "No tiene rol ADMIN")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<AdminResponse>> getAll() {
        List<AdminResponse> response = adminService.getAll()
                .stream()
                .map(adminMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener un admin por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Admin encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "404", description = "No existe un admin con ese id")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AdminResponse> getById(
            @Parameter(description = "Id del admin") @PathVariable String id) {
        Admin admin = adminService.getById(id);
        return ResponseEntity.ok(adminMapper.toResponse(admin));
    }

    @Operation(summary = "Buscar el admin de un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Admin encontrado"),
            @ApiResponse(responseCode = "400", description = "El userId es invalido"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "404", description = "No existe un admin para ese usuario")
    })
    @GetMapping(params = "userId")
    public ResponseEntity<AdminResponse> getByUserId(
            @Parameter(description = "Id del usuario dueño del admin", example = "V1StGXR8_Z5j")
            @RequestParam
            @NotBlank(message = "El userId es obligatorio")
            String userId) {
        // PK compartida: adminId == userId, asi que buscar por userId es getById.
        Admin admin = adminService.getById(userId);
        return ResponseEntity.ok(adminMapper.toResponse(admin));
    }

    // ===== UPDATE =====

    @Operation(summary = "Actualizar un admin por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Admin actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ver el detalle por campo)"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "403", description = "No es el dueño de este admin"),
            @ApiResponse(responseCode = "404", description = "No existe un admin con ese id")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AdminResponse> update(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Id del admin") @PathVariable String id,
            @Valid @RequestBody UpdateAdminRequest request) {
        AuthorizationGuard.requireOwnership(jwt, id);
        Admin updated = adminService.update(id, request);
        return ResponseEntity.ok(adminMapper.toResponse(updated));
    }

    // ===== DELETE =====

    @Operation(summary = "Eliminar un admin por id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Admin eliminado (sin contenido)"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "403", description = "No es el dueño de este admin"),
            @ApiResponse(responseCode = "404", description = "No existe un admin con ese id")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Id del admin") @PathVariable String id) {
        AuthorizationGuard.requireOwnership(jwt, id);
        adminService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
