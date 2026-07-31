package ucu.retojulio2026.talent.universityregistry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ucu.retojulio2026.talent.universityregistry.dto.CreateUniversityRegistryRequest;
import ucu.retojulio2026.talent.universityregistry.dto.UniversityRegistryMapper;
import ucu.retojulio2026.talent.universityregistry.dto.UniversityRegistryResponse;
import ucu.retojulio2026.talent.universityregistry.dto.UpdateUniversityRegistryRequest;

import java.util.List;

@RestController
@RequestMapping("/university-registry")
@Tag(name = "University Registry", description = "Alta, consulta, actualizacion y baja de registros universitarios")
public class UniversityRegistryController {

    private final UniversityRegistryService universityRegistryService;
    private final UniversityRegistryMapper universityRegistryMapper;

    public UniversityRegistryController(UniversityRegistryService universityRegistryService,
                                         UniversityRegistryMapper universityRegistryMapper) {
        this.universityRegistryService = universityRegistryService;
        this.universityRegistryMapper = universityRegistryMapper;
    }

    @Operation(summary = "Crear un registro universitario")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registro creado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ver el detalle por campo)"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
    })
    @PostMapping
    public ResponseEntity<UniversityRegistryResponse> create(@Valid @RequestBody CreateUniversityRegistryRequest request) {
        UniversityRegistry created = universityRegistryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(universityRegistryMapper.toResponse(created));
    }

    @Operation(summary = "Listar todos los registros universitarios")
    @ApiResponse(responseCode = "200", description = "Listado obtenido")
    @GetMapping
    public ResponseEntity<List<UniversityRegistryResponse>> getAll() {
        List<UniversityRegistryResponse> response = universityRegistryService.getAll()
                .stream()
                .map(universityRegistryMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener un registro universitario por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese id")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UniversityRegistryResponse> getById(
            @Parameter(description = "Id del registro") @PathVariable String id) {
        UniversityRegistry universityRegistry = universityRegistryService.getById(id);
        return ResponseEntity.ok(universityRegistryMapper.toResponse(universityRegistry));
    }

    @Operation(summary = "Actualizar los datos de un registro universitario por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ver el detalle por campo)"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese id")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UniversityRegistryResponse> update(
            @Parameter(description = "Id del registro") @PathVariable String id,
            @Valid @RequestBody UpdateUniversityRegistryRequest request) {
        UniversityRegistry updated = universityRegistryService.update(id, request);
        return ResponseEntity.ok(universityRegistryMapper.toResponse(updated));
    }

    @Operation(summary = "Eliminar un registro universitario por id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Registro eliminado (sin contenido)"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese id")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id del registro") @PathVariable String id) {
        universityRegistryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
