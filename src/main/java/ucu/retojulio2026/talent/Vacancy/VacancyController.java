package ucu.retojulio2026.talent.Vacancy;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ucu.retojulio2026.talent.Vacancy.dto.CreateVacancyRequest;
import ucu.retojulio2026.talent.Vacancy.dto.VacancyMapper;
import ucu.retojulio2026.talent.Vacancy.dto.VacancyResponse;

import java.util.List;

@RestController
@RequestMapping("/vacancy")
@Tag(name = "Vacancies", description = "Vacancy management")
public class VacancyController {

    private final VacancyService vacancyService;
    private final VacancyMapper vacancyMapper;

    public VacancyController(VacancyService vacancyService,
                             VacancyMapper vacancyMapper) {
        this.vacancyService = vacancyService;
        this.vacancyMapper = vacancyMapper;
    }

    @Operation(summary = "Get all vacancies")
    @ApiResponse(responseCode = "200", description = "Vacancies retrieved successfully")
    @GetMapping
    public ResponseEntity<List<VacancyResponse>> getAllVacancies() {

        List<VacancyResponse> response = vacancyService.getAllVacancies()
                .stream()
                .map(vacancyMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get vacancy by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vacancy found"),
            @ApiResponse(responseCode = "404", description = "Vacancy not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<VacancyResponse> getVacancyById(
            @Parameter(description = "Vacancy id")
            @PathVariable String id) {

        Vacancy vacancy = vacancyService.getVacancyById(id);
        return ResponseEntity.ok(vacancyMapper.toResponse(vacancy));
    }

    @Operation(summary = "Create vacancy")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vacancy created"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping
    public ResponseEntity<VacancyResponse> create(@Valid @RequestBody CreateVacancyRequest request) {

        Vacancy created = vacancyService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(vacancyMapper.toResponse(created));
    }

    @Operation(summary = "Update vacancy")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vacancy updated"),
            @ApiResponse(responseCode = "404", description = "Vacancy not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<VacancyResponse> updateVacancy(
            @PathVariable String id,
            @Valid @RequestBody CreateVacancyRequest vacancy) {

        Vacancy updated = vacancyService.updateVacancy(id, vacancy);

        return ResponseEntity.ok(vacancyMapper.toResponse(updated));
    }

    @Operation(summary = "Delete vacancy")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Vacancy deleted"),
            @ApiResponse(responseCode = "404", description = "Vacancy not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVacancy(
            @Parameter(description = "Vacancy id")
            @PathVariable String id) {

        vacancyService.deleteVacancy(id);
        return ResponseEntity.noContent().build();
    }
}