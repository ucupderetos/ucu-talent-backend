package ucu.retojulio2026.talent.education;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ucu.retojulio2026.talent.education.dto.CreateEducationRequest;
import ucu.retojulio2026.talent.education.dto.EducationMapper;
import ucu.retojulio2026.talent.education.dto.EducationResponse;
import ucu.retojulio2026.talent.education.dto.GetEducationByIdRequest;
import ucu.retojulio2026.talent.education.dto.UpdateEducationRequest;

import java.util.List;

@RestController
@RequestMapping("/education")
@Validated
@Tag(name = "Educacion", description = "Gestion de educacion de alumnos")
public class EducationController {

    private final EducationService educationService;
    private final EducationMapper educationMapper;

    public EducationController(EducationService educationService, EducationMapper educationMapper) {
        this.educationService = educationService;
        this.educationMapper = educationMapper;
    }

    // ===== CREATE =====

    @Operation(summary = "Crear un registro de educacion")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registro creado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ver el detalle por campo)"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)")
    })
    @PostMapping
    public ResponseEntity<EducationResponse> create(@Valid @RequestBody CreateEducationRequest request) {
        Education created = educationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(educationMapper.toResponse(created));
    }

    // ===== READ =====

    @Operation(summary = "Obtener un registro de educacion por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese id")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EducationResponse> getByEducationId(
            @Parameter(description = "Id de education") @PathVariable("id") String educationId) {
        return ResponseEntity.ok(educationMapper.toResponse(educationService.getByEducationId(educationId)));
    }

    @Operation(summary = "Obtener un registro de educacion por id (DTO request)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro encontrado"),
            @ApiResponse(responseCode = "400", description = "Parametro invalido"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese id")
    })
    @GetMapping("/by-id")
    public ResponseEntity<EducationResponse> getByEducationIdRequest(@Valid @ModelAttribute GetEducationByIdRequest request) {
        return ResponseEntity.ok(educationMapper.toResponse(educationService.getByEducationId(request.educationId())));
    }

    @Operation(summary = "Listar educacion por studentProfileId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "400", description = "Parametro invalido"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)")
    })
    @GetMapping(params = "studentProfileId")
    public ResponseEntity<List<EducationResponse>> getByStudentProfileId(
            @Parameter(description = "Id del perfil alumno")
            @RequestParam
            @NotBlank(message = "studentProfileId es obligatorio")
            String studentProfileId) {
        List<EducationResponse> response = educationService.getByStudentProfileId(studentProfileId)
                .stream()
                .map(educationMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    // ===== UPDATE =====

    @Operation(summary = "Actualizar un registro de educacion por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ver el detalle por campo)"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese id")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EducationResponse> update(
            @Parameter(description = "Id de education") @PathVariable("id") String educationId,
            @Valid @RequestBody UpdateEducationRequest request) {
        Education updated = educationService.update(educationId, request);
        return ResponseEntity.ok(educationMapper.toResponse(updated));
    }

    // ===== DELETE =====

    @Operation(summary = "Eliminar un registro de educacion por id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Registro eliminado"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese id")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id de education") @PathVariable("id") String educationId) {
        educationService.delete(educationId);
        return ResponseEntity.noContent().build();
    }
}
