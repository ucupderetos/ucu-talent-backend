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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ucu.retojulio2026.talent.common.AuthorizationGuard;
import ucu.retojulio2026.talent.common.Department;
import ucu.retojulio2026.talent.company.Company;
import ucu.retojulio2026.talent.company.CompanyService;
import ucu.retojulio2026.talent.vacancy.dto.*;

import java.util.List;

@RestController
@RequestMapping("/vacancy")
@Tag(name = "Puestos", description = "CRUD (Gestión) de un Puesto")
public class VacancyController {

    private final VacancyService
            vacancyService;
    private final VacancyMapper vacancyMapper;
    private final CompanyService companyService;

    public VacancyController(VacancyService vacancyService,
                             VacancyMapper vacancyMapper, CompanyService companyService) {
        this.vacancyService = vacancyService;
        this.vacancyMapper = vacancyMapper;
        this.companyService = companyService;
    }

    // ===== CREATE =====

    @Operation(summary = "Crear Puesto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Puesto creado"),
            @ApiResponse(responseCode = "400", description = "Puesto inválido"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "403", description = "No tiene rol EMPRESA, o la empresa no esta aprobada")
    })
    @PostMapping
    public ResponseEntity<VacancyResponse> create(@Valid @RequestBody CreateVacancyRequest request, @AuthenticationPrincipal Jwt jwt) {
        Company existing = companyService.getById(request.companyId());
        AuthorizationGuard.requireOwnership(jwt, request.companyId());
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
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
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
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
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
            @ApiResponse(responseCode = "400", description = "El companyId es invalido"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
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
            @ApiResponse(responseCode = "400", description = "El areaId es invalido"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
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
            @ApiResponse(responseCode = "400", description = "Modalidad invalida"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
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
            @ApiResponse(responseCode = "400", description = "Localidad invalida"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
    })
    @GetMapping(params = "location")
    public ResponseEntity<List<VacancyResponse>> getByLocation(
            @Parameter(description = "Localidad (departamento)", example = "MONTEVIDEO")
            @RequestParam Department location) {
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
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "403", description = "La empresa ya no esta aprobada"),
            @ApiResponse(responseCode = "404", description = "Puesto no encontrado")
    })

    @PutMapping("/{id}")
    public ResponseEntity<VacancyResponse> updateVacancy(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id,
            @Valid @RequestBody UpdateVacancyRequest vacancy) {
        Vacancy existing = vacancyService.getVacancyById(id);
        AuthorizationGuard.requireOwnership(jwt, existing.getCompanyId());
        Vacancy updated = vacancyService.updateVacancy(id, vacancy);
        return ResponseEntity.ok(vacancyMapper.toResponse(updated));
    }

    @PutMapping("status/{id}")
    public ResponseEntity<VacancyResponse> updateVacancyStatus(
            @AuthenticationPrincipal Jwt jwt, // Solo admin
            @PathVariable String id,
            @Valid @RequestBody UpdateVacancyStatusRequest vacancy) {

        String adminId = jwt.getSubject();
        Vacancy updated = vacancyService.updateVacancyStatus(id, adminId, vacancy);
        return ResponseEntity.ok(vacancyMapper.toResponse(updated));
    }

    // ===== DELETE =====

    @Operation(summary = "Borrar Puesto")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Puesto borrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "403", description = "No tiene rol EMPRESA"),
            @ApiResponse(responseCode = "404", description = "Puesto no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVacancy(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Vacancy id")
            @PathVariable String id) {
        Vacancy existing = vacancyService.getVacancyById(id);
        AuthorizationGuard.requireOwnership(jwt, existing.getCompanyId());
        vacancyService.deleteVacancy(id);
        return ResponseEntity.noContent().build();
    }

    // Sin usar por ahora: derivaba companyId del token en vez del body (fix BOLA/IDOR, ver
    // learning/JWT-step-by-step/08 y 11). Se dejo el metodo listo para reactivarlo despues -
    // para volver a usarlo, agregar "@AuthenticationPrincipal Jwt jwt" a create()/updateVacancy()
    // y llamar a withCompanyId(request, jwt.getSubject()) antes de vacancyService.create(...).
    private CreateVacancyRequest withCompanyId(CreateVacancyRequest request, String companyId) {
        return new CreateVacancyRequest(
                companyId,
                request.areaId(),
                request.publicationDate(),
                request.closingDate(),
                request.location(),
                request.modality(),
                request.name(),
                request.description(),
                request.requirements(),
                request.contractType(),
                request.salaryRange()
        );
    }
}
