package ucu.retojulio2026.talent.vacancy;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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