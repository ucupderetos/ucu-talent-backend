package ucu.retojulio2026.talent.company;

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
import ucu.retojulio2026.talent.company.dto.CreateCompanyRequest;
import ucu.retojulio2026.talent.company.dto.UpdateCompanyRequest;
import ucu.retojulio2026.talent.company.dto.CompanyMapper;
import ucu.retojulio2026.talent.company.dto.CompanyResponse;

import java.util.List;

@RestController
@RequestMapping("/company")
@Tag(name = "Empresas", description = "Alta, consulta y baja de empresas")
public class CompanyController {

    private final CompanyService companyService;
    private final CompanyDeletionService companyDeletionService;
    private final CompanyMapper companyMapper;

    public CompanyController(CompanyService companyService, CompanyDeletionService companyDeletionService,
            CompanyMapper companyMapper) {
        this.companyService = companyService;
        this.companyDeletionService = companyDeletionService;
        this.companyMapper = companyMapper;
    }

    // ===== CREATE =====

    @Operation(summary = "Crear una empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Empresa creada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ver el detalle por campo)"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)")
    })
    @PostMapping
    public ResponseEntity<CompanyResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateCompanyRequest request) {
        // userId siempre sale del token, nunca del body (PK compartida: companyId == userId).
        CreateCompanyRequest ownRequest = new CreateCompanyRequest(jwt.getSubject(), request.industry(),
                request.description(), request.webUrl(), request.linkedinUrl(), request.location());
        Company created = companyService.create(ownRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(companyMapper.toResponse(created));
    }

    // ===== READ =====

    @Operation(summary = "Listar todas las empresas")
    @ApiResponse(responseCode = "200", description = "Listado obtenido")
    @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)")
    @GetMapping
    public ResponseEntity<List<CompanyResponse>> getAll() {
        List<CompanyResponse> response = companyService.getAll()
                .stream()
                .map(companyMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener una empresa por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "404", description = "No existe una empresa con ese id")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getById(
            @Parameter(description = "Id de la empresa") @PathVariable String id) {
        Company company = companyService.getById(id);
        return ResponseEntity.ok(companyMapper.toResponse(company));
    }

    @Operation(summary = "Buscar la empresa de un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa encontrada"),
            @ApiResponse(responseCode = "400", description = "El userId es invalido"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "404", description = "No existe una empresa para ese usuario")
    })
    @GetMapping(params = "userId")
    public ResponseEntity<CompanyResponse> getByUserId(
            @Parameter(description = "Id del usuario dueño de la empresa", example = "V1StGXR8_Z5j")
            @RequestParam
            @NotBlank(message = "El userId es obligatorio")
            String userId) {
        Company company = companyService.getByUserId(userId);
        return ResponseEntity.ok(companyMapper.toResponse(company));
    }

    // ===== UPDATE =====

    @Operation(summary = "Actualizar una empresa por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ver el detalle por campo)"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "403", description = "Usuario autenticado no tiene permisos para modificar esta recurso."),
            @ApiResponse(responseCode = "404", description = "No existe una empresa con ese id")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponse> update(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Id de la empresa") @PathVariable String id,
            @Valid @RequestBody UpdateCompanyRequest request)
    {
        AuthorizationGuard.requireOwnership(jwt, id);
        Company updated = companyService.update(id, request);
        return ResponseEntity.ok(companyMapper.toResponse(updated));
    }

    // ===== DELETE =====

    @Operation(summary = "Eliminar una empresa por id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Empresa eliminada (sin contenido)"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "403", description = "Usuario autenticado no tiene permisos para eliminar esta empresa."),
            @ApiResponse(responseCode = "404", description = "No existe una empresa con ese id")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Id de la empresa") @PathVariable String id) {
        AuthorizationGuard.requireOwnership(jwt, id);
        companyDeletionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
