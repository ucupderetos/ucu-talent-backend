package ucu.retojulio2026.talent.workexperience;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ucu.retojulio2026.talent.common.ForbiddenOperationException;
import ucu.retojulio2026.talent.workexperience.dto.*;

import java.util.List;

@RestController
@RequestMapping("/work-experience")
@Validated
@Tag(name = "Experiencia laboral", description = "Gestion de experiencia laboral de alumnos")
public class WorkExperienceController {

    private final WorkExperienceService workExperienceService;
    private final WorkExperienceMapper workExperienceMapper;

    public WorkExperienceController(WorkExperienceService workExperienceService,
                                    WorkExperienceMapper workExperienceMapper) {
        this.workExperienceService = workExperienceService;
        this.workExperienceMapper = workExperienceMapper;
    }

    // ===== CREATE =====

    @Operation(summary = "Crear una experiencia laboral")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Experiencia creada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ver el detalle por campo)"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "403", description = "Usuario autenticado no tiene permisos para modificar esta recurso.")
    })
    @PostMapping
    public ResponseEntity<WorkExperienceResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateWorkExperienceRequest request) {
        requireOwnership(jwt, request.studentProfileId());
        WorkExperience created = workExperienceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(workExperienceMapper.toResponse(created));
    }

    // ===== READ =====

    @Operation(summary = "Obtener una experiencia laboral por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Experiencia encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe una experiencia con ese id"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)")
    })
    @GetMapping("/{id}")
    public ResponseEntity<WorkExperienceResponse> getById(
            @Parameter(description = "Id de workExperience") @PathVariable String id) {
        return ResponseEntity.ok(workExperienceMapper.toResponse(workExperienceService.getById(id)));
    }

    @Operation(summary = "Listar experiencia laboral por studentProfileId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "400", description = "Parametro invalido"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)")
    })
    @GetMapping(params = "studentProfileId")
    public ResponseEntity<List<WorkExperienceResponse>> getByStudentProfileId(
            @Parameter(description = "Id del perfil alumno")
            @RequestParam
            @NotBlank(message = "studentProfileId es obligatorio")
            String studentProfileId) {
        List<WorkExperienceResponse> response = workExperienceService.getByStudentProfileId(studentProfileId)
                .stream()
                .map(workExperienceMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    // ===== UPDATE =====

    @Operation(summary = "Actualizar una experiencia laboral por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Experiencia actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ver el detalle por campo)"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "403", description = "Usuario autenticado no tiene permisos para modificar esta recurso."),
            @ApiResponse(responseCode = "404", description = "No existe una experiencia con ese id")
    })
    @PutMapping("/{id}")
    public ResponseEntity<WorkExperienceResponse> update(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Id de workExperience") @PathVariable String id,
            @Valid @RequestBody UpdateWorkExperienceRequest request) {
        WorkExperience existing = workExperienceService.getById(id); // 404 si no existe
        requireOwnership(jwt, existing.getStudentProfileId());
        WorkExperience updated = workExperienceService.update(id, request);
        return ResponseEntity.ok(workExperienceMapper.toResponse(updated));
    }

    // ===== DELETE =====

    @Operation(summary = "Eliminar una experiencia laboral por id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Experiencia eliminada"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "403", description = "Usuario autenticado no tiene permisos para modificar esta recurso."),
            @ApiResponse(responseCode = "404", description = "No existe una experiencia con ese id")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Id de workExperience") @PathVariable String id) {
        WorkExperience existing = workExperienceService.getById(id);
        requireOwnership(jwt, existing.getStudentProfileId());
        workExperienceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void requireOwnership(Jwt jwt, String targetUserId) {
        boolean isSelf = jwt.getSubject().equals(targetUserId);
        if (!isSelf) {
            throw new ForbiddenOperationException("Usuario autenticado no tiene permisos para modificar esta recurso.");
        }
    }
}
