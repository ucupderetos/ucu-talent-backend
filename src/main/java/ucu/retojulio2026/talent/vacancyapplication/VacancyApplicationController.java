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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import ucu.retojulio2026.talent.vacancyapplication.dto.CreateVacancyApplicationRequest;
import ucu.retojulio2026.talent.vacancyapplication.dto.VacancyApplicationMapper;
import ucu.retojulio2026.talent.vacancyapplication.dto.VacancyApplicationResponse;
import ucu.retojulio2026.talent.vacancyapplication.dto.UpdateVacancyApplicationRequest;

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

    @Operation(summary = "Obtener una postulación por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Postulación encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe una postulación con ese id")
    })
    @GetMapping("/{id}")
    public ResponseEntity<VacancyApplicationResponse> getById(
            @Parameter(description = "Id de la postulación (NanoID de 12 caracteres)") @PathVariable String id) {
        VacancyApplication vacancyApplication = vacancyApplicationService.getById(id);
        return ResponseEntity.ok(vacancyApplicationMapper.toResponse(vacancyApplication));
    }

    @Operation(summary = "Crear una postulación")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Postulación creada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ver el detalle por campo)"),
            @ApiResponse(responseCode = "404", description = "No existe la vacante o el perfil de alumno")
    })
    @PostMapping
    public ResponseEntity<VacancyApplicationResponse> create(
            @Valid @RequestBody CreateVacancyApplicationRequest request) {
        VacancyApplication created = vacancyApplicationService.create(vacancyApplicationMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(vacancyApplicationMapper.toResponse(created));
    }

    @Operation(summary = "Actualizar el estado de una postulación por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Postulación actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ver el detalle por campo)"),
            @ApiResponse(responseCode = "404", description = "No existe una postulación con ese id")
    })
    @PutMapping("/{id}")
    public ResponseEntity<VacancyApplicationResponse> update(
            @Parameter(description = "Id de la postulación (NanoID de 12 caracteres)") @PathVariable String id,
            @Valid @RequestBody UpdateVacancyApplicationRequest request) {
        VacancyApplication updated = vacancyApplicationService.update(id, request.status());
        return ResponseEntity.ok(vacancyApplicationMapper.toResponse(updated));
    }

    @Operation(summary = "Eliminar una postulación por id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Postulación eliminada (sin contenido)"),
            @ApiResponse(responseCode = "404", description = "No existe una postulación con ese id")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id de la postulación (NanoID de 12 caracteres)") @PathVariable String id) {
        vacancyApplicationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
