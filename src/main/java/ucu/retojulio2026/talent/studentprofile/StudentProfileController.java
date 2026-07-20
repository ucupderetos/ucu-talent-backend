package ucu.retojulio2026.talent.studentprofile;

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
import org.springframework.web.bind.annotation.*;

import ucu.retojulio2026.talent.common.AuthorizationGuard;
import ucu.retojulio2026.talent.studentprofile.dto.CreateStudentProfileRequest;
import ucu.retojulio2026.talent.studentprofile.dto.StudentProfileMapper;
import ucu.retojulio2026.talent.studentprofile.dto.StudentProfileResponse;

import java.util.List;

@RestController
@RequestMapping("/student-profile")
@Tag(name = "Alumnos", description = "Alta, consulta y baja de perfiles de alumno") // agrupa los endpoints en Swagger UI
public class StudentProfileController {

    private final StudentProfileService studentProfileService;
    private final StudentProfileMapper studentProfileMapper;

    public StudentProfileController(StudentProfileService studentProfileService, StudentProfileMapper studentProfileMapper) {
        this.studentProfileService = studentProfileService;
        this.studentProfileMapper = studentProfileMapper;
    }

    // ===== CREATE =====

    @Operation(summary = "Crear un perfil de alumno")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Perfil creado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ver el detalle por campo)"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "403", description = "No tiene rol ALUMNO"),
            @ApiResponse(responseCode = "409", description = "El usuario ya tiene un perfil de alumno")
    })
    @PostMapping
    public ResponseEntity<StudentProfileResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateStudentProfileRequest request) {
        CreateStudentProfileRequest ownRequest = new CreateStudentProfileRequest(jwt.getSubject(), request.skills());
        StudentProfile created = studentProfileService.create(ownRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(studentProfileMapper.toResponse(created));
    }

    // ===== READ =====

    @Operation(summary = "Listar todos los perfiles de alumno")
    @ApiResponse(responseCode = "200", description = "Listado obtenido")
    @GetMapping
    public ResponseEntity<List<StudentProfileResponse>> getAll() {
        List<StudentProfileResponse> response = studentProfileService.getAll()
                .stream()
                .map(studentProfileMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener un perfil de alumno por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "404", description = "No existe un perfil con ese id")
    })
    @GetMapping("/{id}")
    public ResponseEntity<StudentProfileResponse> getById(
            @Parameter(description = "Id del perfil de alumno") @PathVariable String id) {
        StudentProfile studentProfile = studentProfileService.getById(id);
        return ResponseEntity.ok(studentProfileMapper.toResponse(studentProfile));
    }

    @Operation(summary = "Buscar el perfil de alumno de un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil encontrado"),
            @ApiResponse(responseCode = "400", description = "El userId es invalido"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "404", description = "No existe un perfil para ese usuario")
    })
    @GetMapping(params = "userId")
    public ResponseEntity<StudentProfileResponse> getByUserId(
            @Parameter(description = "Id del usuario dueño del perfil", example = "V1StGXR8_Z5j")
            @RequestParam
            @NotBlank(message = "El userId es obligatorio")
            String userId) {
        StudentProfile studentProfile = studentProfileService.getByUserId(userId);
        return ResponseEntity.ok(studentProfileMapper.toResponse(studentProfile));
    }

    // ===== UPDATE =====


    // ===== DELETE =====

    @Operation(summary = "Eliminar un perfil de alumno por id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Perfil eliminado (sin contenido)"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "403", description = "No es el dueño de este perfil"),
            @ApiResponse(responseCode = "404", description = "No existe un perfil con ese id")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Id del perfil de alumno") @PathVariable String id) {
        AuthorizationGuard.requireOwnership(jwt, id);
        studentProfileService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
