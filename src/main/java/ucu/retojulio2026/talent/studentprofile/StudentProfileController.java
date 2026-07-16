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
import org.springframework.web.bind.annotation.*;

import ucu.retojulio2026.talent.studentprofile.dto.CreateStudentProfileRequest;
import ucu.retojulio2026.talent.studentprofile.dto.StudentProfileMapper;
import ucu.retojulio2026.talent.studentprofile.dto.StudentProfileResponse;

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

    @Operation(summary = "Obtener un perfil de alumno por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un perfil con ese id")
    })
    @GetMapping("/{id}")
    public ResponseEntity<StudentProfileResponse> getById(
            @Parameter(description = "Id del perfil de alumno (NanoID de 12 caracteres)") @PathVariable String id) {
        StudentProfile studentProfile = studentProfileService.getById(id);
        return ResponseEntity.ok(studentProfileMapper.toResponse(studentProfile));
    }

    @Operation(summary = "Buscar el perfil de alumno de un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil encontrado"),
            @ApiResponse(responseCode = "400", description = "El userId es invalido"),
            @ApiResponse(responseCode = "404", description = "No existe un perfil para ese usuario")
    })
    @GetMapping(params = "userId")
    public ResponseEntity<StudentProfileResponse> getByUserId(
            @Parameter(description = "Id del usuario dueño del perfil (NanoID de 12 caracteres)", example = "V1StGXR8_Z5j")
            @RequestParam
            @NotBlank(message = "El userId es obligatorio")
            String userId) {
        StudentProfile studentProfile = studentProfileService.getByUserId(userId);
        return ResponseEntity.ok(studentProfileMapper.toResponse(studentProfile));
    }

    @Operation(summary = "Crear un perfil de alumno")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Perfil creado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ver el detalle por campo)")
    })
    @PostMapping
    public ResponseEntity<StudentProfileResponse> create(@Valid @RequestBody CreateStudentProfileRequest request) {
        StudentProfile created = studentProfileService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(studentProfileMapper.toResponse(created));
    }

    @Operation(summary = "Eliminar un perfil de alumno por id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Perfil eliminado (sin contenido)"),
            @ApiResponse(responseCode = "404", description = "No existe un perfil con ese id")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id del perfil de alumno (NanoID de 12 caracteres)") @PathVariable String id) {
        studentProfileService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
