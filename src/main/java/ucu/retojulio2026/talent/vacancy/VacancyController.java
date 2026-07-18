package ucu.retojulio2026.talent.vacancy;

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
import ucu.retojulio2026.talent.vacancy.dto.CreateVacancyRequest;
import ucu.retojulio2026.talent.vacancy.dto.VacancyMapper;
import ucu.retojulio2026.talent.vacancy.dto.VacancyResponse;

import java.util.List;

@RestController
@RequestMapping("/vacancy")
@Tag(name = "Puestos", description = "CRUD (Gestión) de un Puesto")
public class VacancyController {

    private final VacancyService vacancyService;
    private final VacancyMapper vacancyMapper;

    public VacancyController(VacancyService vacancyService,
                             VacancyMapper vacancyMapper) {
        this.vacancyService = vacancyService;
        this.vacancyMapper = vacancyMapper;
    }

    // ===== CREATE =====

    @Operation(summary = "Crear Puesto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Puesto creado"),
            @ApiResponse(responseCode = "400", description = "Puesto inválido")
    })
    @PostMapping
    public ResponseEntity<VacancyResponse> create(@Valid @RequestBody CreateVacancyRequest request) {

        Vacancy created = vacancyService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(vacancyMapper.toResponse(created));
    }

    // ===== READ =====

    @Operation(summary = "Obtener todos los Puestos")
    @ApiResponse(responseCode = "200", description = "Puestos encontrados")
    @GetMapping
    public ResponseEntity<List<VacancyResponse>> getAllVacancies() {

        List<VacancyResponse> response = vacancyService.getAllVacancies()
                .stream()
                .map(vacancyMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener el Puesto por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Puesto encontrado"),
            @ApiResponse(responseCode = "404", description = "Puesto no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<VacancyResponse> getVacancyById(
            @Parameter(description = "Vacancy id")
            @PathVariable String id) {

        Vacancy vacancy = vacancyService.getVacancyById(id);
        return ResponseEntity.ok(vacancyMapper.toResponse(vacancy));
    }

    @Operation(summary = "Listar Puestos por estado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "400", description = "Estado invalido")
    })
    @GetMapping(params = "status")
    public ResponseEntity<List<VacancyResponse>> getByStatus(
            @Parameter(description = "Estado del puesto", example = "PENDIENTE")
            @RequestParam VacancyStatus status) {
        List<VacancyResponse> response = vacancyService.getByStatus(status)
                .stream()
                .map(vacancyMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar Puestos por empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "400", description = "El companyId es invalido")
    })
    @GetMapping(params = "companyId")
    public ResponseEntity<List<VacancyResponse>> getByCompanyId(
            @Parameter(description = "Id de la empresa", example = "V1StGXR8_Z5j")
            @RequestParam
            @NotBlank(message = "El companyId es obligatorio")
            String companyId) {
        List<VacancyResponse> response = vacancyService.getByCompanyId(companyId)
                .stream()
                .map(vacancyMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar Puestos por area")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "400", description = "El areaId es invalido")
    })
    @GetMapping(params = "areaId")
    public ResponseEntity<List<VacancyResponse>> getByAreaId(
            @Parameter(description = "Id del area", example = "V1StGXR8_Z5j")
            @RequestParam
            @NotBlank(message = "El areaId es obligatorio")
            String areaId) {
        List<VacancyResponse> response = vacancyService.getByAreaId(areaId)
                .stream()
                .map(vacancyMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar Puestos por modalidad")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "400", description = "Modalidad invalida")
    })
    @GetMapping(params = "modality")
    public ResponseEntity<List<VacancyResponse>> getByModality(
            @Parameter(description = "Modalidad de trabajo", example = "REMOTO")
            @RequestParam Modality modality) {
        List<VacancyResponse> response = vacancyService.getByModality(modality)
                .stream()
                .map(vacancyMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar Puestos por localidad")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "400", description = "Localidad invalida")
    })
    @GetMapping(params = "location")
    public ResponseEntity<List<VacancyResponse>> getByLocation(
            @Parameter(description = "Localidad (departamento)", example = "MONTEVIDEO")
            @RequestParam Departamento location) {
        List<VacancyResponse> response = vacancyService.getByLocation(location)
                .stream()
                .map(vacancyMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    // ===== UPDATE =====

    @Operation(summary = "Actualizar Puesto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Puesto actualizado"),
            @ApiResponse(responseCode = "404", description = "Puesto no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<VacancyResponse> updateVacancy(
            @PathVariable String id,
            @Valid @RequestBody CreateVacancyRequest vacancy) {

        Vacancy updated = vacancyService.updateVacancy(id, vacancy);

        return ResponseEntity.ok(vacancyMapper.toResponse(updated));
    }

    // ===== DELETE =====

    @Operation(summary = "Borrar Puesto")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Puesto borrado"),
            @ApiResponse(responseCode = "404", description = "Puesto no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVacancy(
            @Parameter(description = "Vacancy id")
            @PathVariable String id) {

        vacancyService.deleteVacancy(id);
        return ResponseEntity.noContent().build();
    }
}
