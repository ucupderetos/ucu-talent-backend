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
import org.springframework.web.bind.annotation.*;

import ucu.retojulio2026.talent.company.dto.CreateCompanyRequest;
import ucu.retojulio2026.talent.company.dto.UpdateCompanyRequest;
import ucu.retojulio2026.talent.company.dto.CompanyMapper;
import ucu.retojulio2026.talent.company.dto.CompanyResponse;

@RestController
@RequestMapping("/company")
@Tag(name = "Empresas", description = "Alta, consulta y baja de empresas") // agrupa los endpoints en Swagger UI
public class CompanyController {

    private final CompanyService companyService;
    private final CompanyMapper companyMapper;

    public CompanyController(CompanyService companyService, CompanyMapper companyMapper) {
        this.companyService = companyService;
        this.companyMapper = companyMapper;
    }

    @Operation(summary = "Obtener una empresa por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe una empresa con ese id")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getById(
            @Parameter(description = "Id de la empresa (NanoID de 12 caracteres)") @PathVariable String id) {
        Company company = companyService.getById(id);
        return ResponseEntity.ok(companyMapper.toResponse(company));
    }

    @Operation(summary = "Buscar la empresa de un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa encontrada"),
            @ApiResponse(responseCode = "400", description = "El userId es invalido"),
            @ApiResponse(responseCode = "404", description = "No existe una empresa para ese usuario")
    })
    @GetMapping(params = "userId")
    public ResponseEntity<CompanyResponse> getByUserId(
            @Parameter(description = "Id del usuario dueño de la empresa (NanoID de 12 caracteres)", example = "V1StGXR8_Z5j")
            @RequestParam
            @NotBlank(message = "El userId es obligatorio")
            String userId) {
        Company company = companyService.getByUserId(userId);
        return ResponseEntity.ok(companyMapper.toResponse(company));
    }

    @Operation(summary = "Crear una empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Empresa creada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ver el detalle por campo)")
    })
    @PostMapping
    public ResponseEntity<CompanyResponse> create(@Valid @RequestBody CreateCompanyRequest request) {
        Company created = companyService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(companyMapper.toResponse(created));
    }

    @Operation(summary = "Actualizar una empresa por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ver el detalle por campo)"),
            @ApiResponse(responseCode = "404", description = "No existe una empresa con ese id")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponse> update(
            @Parameter(description = "Id de la empresa (NanoID de 12 caracteres)") @PathVariable String id,
            @Valid @RequestBody UpdateCompanyRequest request) {
        Company updated = companyService.update(id, request);
        return ResponseEntity.ok(companyMapper.toResponse(updated));
    }

    @Operation(summary = "Eliminar una empresa por id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Empresa eliminada (sin contenido)"),
            @ApiResponse(responseCode = "404", description = "No existe una empresa con ese id")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id de la empresa (NanoID de 12 caracteres)") @PathVariable String id) {
        companyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
