package ucu.retojulio2026.talent.vacancy;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ucu.retojulio2026.talent.common.AuthorizationGuard;
import ucu.retojulio2026.talent.common.Department;
import ucu.retojulio2026.talent.company.Company;
import ucu.retojulio2026.talent.company.CompanyService;
import ucu.retojulio2026.talent.vacancy.dto.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import ucu.retojulio2026.talent.vacancy.dto.SearchCriteriaVacancyRequest;
import ucu.retojulio2026.talent.vacancy.filter.VacancySortField;

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
        AuthorizationGuard.requireOwnership(jwt, existing.getCompanyId());
        Vacancy created = vacancyService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(vacancyMapper.toResponse(created));
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

    @Operation(summary = "Obtener el Puesto por id con la empresa y el area resueltos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Puesto encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "404", description = "Puesto no encontrado")
    })
    @GetMapping("/{id}/resolved")
    public ResponseEntity<ResolvedVacancyResponse> getResolvedVacancyById(
            @Parameter(description = "Vacancy id")
            @PathVariable String id) {
        return ResponseEntity.ok(vacancyService.getResolvedById(id));
    }

    @Operation(summary = "Busqueda combinada de Puestos (ADMIN)",
            description = "Filtros combinables (AND): area, tipo de contrato, modalidad, localidad y "
                    + "keyword (busca en nombre y descripcion). Todos los filtros son opcionales.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<VacancyResponse>> search(
            @Parameter(description = "Estado") @RequestParam(required = false) VacancyStatus status,
            @Parameter(description = "Id del area (incluye subareas)") @RequestParam(required = false) String areaId,
            @Parameter(description = "Id de la carrera (se resuelve via su area)") @RequestParam(required = false) String degreeId,
            @Parameter(description = "Tipo de contrato") @RequestParam(required = false) String contractType,
            @Parameter(description = "Modalidad de trabajo") @RequestParam(required = false) Modality modality,
            @Parameter(description = "Localidad (departamento)") @RequestParam(required = false) Department location,
            @Parameter(description = "Palabra clave, busca en nombre y descripcion") @RequestParam(required = false) String keyword,
            @Parameter(description = "Campo de orden") @RequestParam(required = false, defaultValue = "PUBLICATION_DATE") VacancySortField sortBy,
            @Parameter(description = "Direccion del orden") @RequestParam(required = false, defaultValue = "DESC") Sort.Direction sortDirection,
            @Parameter(description = "Numero de pagina (0-indexed)") @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Tamaño de pagina") @RequestParam(required = false, defaultValue = "20") int size,
            @Parameter(description = "Eliminado") @RequestParam(required = false) Boolean deleted)
        {

        SearchCriteriaVacancyRequest criteria = new SearchCriteriaVacancyRequest(
                areaId, degreeId, contractType, modality, location, keyword, status, deleted);
        Sort sort = Sort.by(sortDirection, sortBy.propertyName());
        Page<VacancyResponse> response = vacancyService.search(criteria, PageRequest.of(page, size, sort))
                .map(vacancyMapper::toResponse);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Busqueda combinada de Puestos",
            description = "Filtros combinables (AND): area, tipo de contrato, modalidad, localidad y "
                    + "keyword (busca en nombre y descripcion). Todos los filtros son opcionales.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)")
    })
    @GetMapping("student/search")
    public ResponseEntity<Page<VacancyStudentResponse>> searchStudent(
            @Parameter(description = "Id del area (incluye subareas)") @RequestParam(required = false) String areaId,
            @Parameter(description = "Id de la carrera (se resuelve via su area)") @RequestParam(required = false) String degreeId,
            @Parameter(description = "Tipo de contrato") @RequestParam(required = false) String contractType,
            @Parameter(description = "Modalidad de trabajo") @RequestParam(required = false) Modality modality,
            @Parameter(description = "Localidad (departamento)") @RequestParam(required = false) Department location,
            @Parameter(description = "Palabra clave, busca en nombre y descripcion") @RequestParam(required = false) String keyword,
            @Parameter(description = "Campo de orden") @RequestParam(required = false, defaultValue = "PUBLICATION_DATE") VacancySortField sortBy,
            @Parameter(description = "Direccion del orden") @RequestParam(required = false, defaultValue = "DESC") Sort.Direction sortDirection,
            @Parameter(description = "Numero de pagina (0-indexed)") @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Tamaño de pagina") @RequestParam(required = false, defaultValue = "20") int size) {

        SearchCriteriaVacancyRequest criteria = new SearchCriteriaVacancyRequest(
                areaId, degreeId, contractType, modality, location, keyword, null, false);
        Sort sort = Sort.by(sortDirection, sortBy.propertyName());
        Page<VacancyStudentResponse> response = vacancyService.searchPublished(criteria, PageRequest.of(page, size, sort))
                .map(vacancyMapper::toStudentResponse);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar Puestos por estado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "400", description = "Estado invalido")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<VacancyResponse>> getByStatus(
            @Parameter(description = "Estado del puesto", example = "PENDIENTE")
            @PathVariable VacancyStatus status) {
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
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<VacancyResponse>> getByCompanyId(
            @Parameter(description = "Id de la empresa", example = "V1StGXR8_Z5j")
            @PathVariable
            @NotBlank(message = "El companyId es obligatorio")
            String companyId) {
        List<VacancyResponse> response = vacancyService.getByCompanyId(companyId)
                .stream()
                .map(vacancyMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar Puestos de una empresa para gestión")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "400", description = "El companyId es invalido"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "403", description = "No es la empresa dueña ni tiene rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "La empresa no existe"),
    })
    @GetMapping("/company/{companyId}/management")
    public ResponseEntity<List<VacancyManagementResponse>> getManagementByCompanyId(
            @Parameter(description = "Id de la empresa", example = "V1StGXR8_Z5j")
            @PathVariable
            @NotBlank(message = "El companyId es obligatorio")
            String companyId,
            @AuthenticationPrincipal Jwt jwt) {
        AuthorizationGuard.requireOwnershipOrRoles(jwt, companyId, "ADMIN");
        return ResponseEntity.ok(vacancyService.getManagementByCompanyId(companyId));
    }

    @Operation(summary = "Listar Puestos por area")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "400", description = "El areaId es invalido"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
    })
    @GetMapping("/area/{areaId}")
    public ResponseEntity<List<VacancyResponse>> getByAreaId(
            @Parameter(description = "Id del area", example = "V1StGXR8_Z5j")
            @PathVariable
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
    @GetMapping("/modality/{modality}")
    public ResponseEntity<List<VacancyResponse>> getByModality(
            @Parameter(description = "Modalidad de trabajo", example = "REMOTO")
            @PathVariable Modality modality) {
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
    @GetMapping("/location/{location}")
    public ResponseEntity<List<VacancyResponse>> getByLocation(
            @Parameter(description = "Localidad (departamento)", example = "MONTEVIDEO")
            @PathVariable Department location) {
        List<VacancyResponse> response = vacancyService.getByLocation(location)
                .stream()
                .map(vacancyMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Totales de puestos por estado (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Totales obtenidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "403", description = "No tiene rol ADMIN"),
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status-summary")
    public ResponseEntity<VacancyStatusSummaryResponse> getStatusSummary() {
        return ResponseEntity.ok(VacancyStatusSummaryResponse.from(vacancyService.countByStatusSummary()));
    }

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

    @Operation(summary = "Actualizar Estado del Puesto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Puesto actualizado"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "403", description = "La empresa ya no esta aprobada"),
            @ApiResponse(responseCode = "404", description = "Puesto no encontrado")
    })
    @PatchMapping("status/{id}")
    public ResponseEntity<VacancyResponse> updateVacancyStatusCompany(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id,
            @Valid @RequestBody UpdateVacancyStatusRequest vacancy) {
        Vacancy existing = vacancyService.getVacancyById(id);
        AuthorizationGuard.requireOwnership(jwt, existing.getCompanyId());
        Vacancy updated = vacancyService.updateVacancyStatus(id, vacancy);
        return ResponseEntity.ok(vacancyMapper.toResponse(updated));
    }

    @Operation(summary = "Actualizar Estado del Puesto (ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Puesto actualizado"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "403", description = "La empresa ya no esta aprobada"),
            @ApiResponse(responseCode = "404", description = "Puesto no encontrado")
    })
    @PutMapping("status/{id}")
    public ResponseEntity<VacancyResponse> updateVacancyStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id,
            @Valid @RequestBody UpdateVacancyStatusAdminRequest vacancy) {
        String adminId = jwt.getSubject();
        Vacancy updated = vacancyService.updateVacancyStatusAdmin(id, adminId, vacancy);
        return ResponseEntity.ok(vacancyMapper.toResponse(updated));
    }

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
                request.salary()
        );
    }
}
