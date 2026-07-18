package ucu.retojulio2026.talent.vacancyapplication;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import ucu.retojulio2026.talent.vacancyapplication.dto.CreateVacancyApplicationRequest;
import ucu.retojulio2026.talent.vacancyapplication.dto.VacancyApplicationMapper;
import ucu.retojulio2026.talent.vacancyapplication.dto.VacancyApplicationResponse;
import ucu.retojulio2026.talent.vacancyapplication.dto.UpdateVacancyApplicationRequest;

import java.util.List;

@RestController
@RequestMapping("/vacancy-application")
@Tag(name = "Postulaciones", description = "Alta, consulta y baja de postulaciones a vacantes")
public class VacancyApplicationController {

    private final VacancyApplicationService vacancyApplicationService;
    private final VacancyApplicationMapper vacancyApplicationMapper;

    public VacancyApplicationController(VacancyApplicationService vacancyApplicationService,
                                        VacancyApplicationMapper vacancyApplicationMapper) {
        this.vacancyApplicationService = vacancyApplicationService;
        this.vacancyApplicationMapper = vacancyApplicationMapper;
    }

    // ===== CREATE =====

    @Operation(summary = "Crear una postulación")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Postulación creada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ver el detalle por campo)"),
            @ApiResponse(responseCode = "404", description = "No existe la vacante o el perfil de alumno")
    })
    @PostMapping
    public ResponseEntity<VacancyApplicationResponse> create(
            @Valid @RequestBody CreateVacancyApplicationRequest request) {
        VacancyApplication created = vacancyApplicationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(vacancyApplicationMapper.toResponse(created));
    }

    // ===== READ =====

    @Operation(summary = "Obtener una postulación por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Postulación encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe una postulación con ese id")
    })
    @GetMapping("/{id}")
    public ResponseEntity<VacancyApplicationResponse> getById(
            @Parameter(description = "Id de la postulación") @PathVariable String id) {
        VacancyApplication vacancyApplication = vacancyApplicationService.getById(id);
        return ResponseEntity.ok(vacancyApplicationMapper.toResponse(vacancyApplication));
    }

    @Operation(summary = "Listar todas las postulaciones")
    @ApiResponse(responseCode = "200", description = "Listado obtenido")
    @GetMapping
    public ResponseEntity<List<VacancyApplicationResponse>> getAll() {
        List<VacancyApplicationResponse> response = vacancyApplicationService.getAll()
                .stream()
                .map(vacancyApplicationMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar postulaciones por vacante")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "400", description = "El vacancyId es invalido")
    })
    @GetMapping(params = "vacancyId")
    public ResponseEntity<List<VacancyApplicationResponse>> getByVacancyId(
            @Parameter(description = "Id de la vacante", example = "V1StGXR8_Z5j")
            @RequestParam
            @NotBlank(message = "El vacancyId es obligatorio")
            String vacancyId) {
        List<VacancyApplicationResponse> response = vacancyApplicationService.getByVacancyId(vacancyId)
                .stream()
                .map(vacancyApplicationMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar postulaciones por perfil de alumno")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "400", description = "El studentProfileId es invalido")
    })
    @GetMapping(params = "studentProfileId")
    public ResponseEntity<List<VacancyApplicationResponse>> getByStudentProfileId(
            @Parameter(description = "Id del perfil de alumno", example = "V1StGXR8_Z5j")
            @RequestParam
            @NotBlank(message = "El studentProfileId es obligatorio")
            String studentProfileId) {
        List<VacancyApplicationResponse> response = vacancyApplicationService.getByStudentProfileId(studentProfileId)
                .stream()
                .map(vacancyApplicationMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar postulaciones por estado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "400", description = "Estado invalido")
    })
    @GetMapping(params = "status")
    public ResponseEntity<List<VacancyApplicationResponse>> getByStatus(
            @Parameter(description = "Estado de la postulación", example = "PENDIENTE")
            @RequestParam VacancyApplicationStatus status) {
        List<VacancyApplicationResponse> response = vacancyApplicationService.getByStatus(status)
                .stream()
                .map(vacancyApplicationMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    // ===== UPDATE =====

    @Operation(summary = "Actualizar el estado de una postulación por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Postulación actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ver el detalle por campo)"),
            @ApiResponse(responseCode = "404", description = "No existe una postulación con ese id")
    })
    @PutMapping("/{id}")
    public ResponseEntity<VacancyApplicationResponse> update(
            @Parameter(description = "Id de la postulación") @PathVariable String id,
            @Valid @RequestBody UpdateVacancyApplicationRequest request) {
        VacancyApplication updated = vacancyApplicationService.update(id, request.status());
        return ResponseEntity.ok(vacancyApplicationMapper.toResponse(updated));
    }

    // ===== DELETE =====

    @Operation(summary = "Eliminar una postulación por id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Postulación eliminada (sin contenido)"),
            @ApiResponse(responseCode = "404", description = "No existe una postulación con ese id")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id de la postulación") @PathVariable String id) {
        vacancyApplicationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
