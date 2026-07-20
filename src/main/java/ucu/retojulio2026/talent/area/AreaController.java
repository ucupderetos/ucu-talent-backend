package ucu.retojulio2026.talent.area;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ucu.retojulio2026.talent.area.dto.CreateAreaRequest;
import ucu.retojulio2026.talent.area.dto.UpdateAreaRequest;
import ucu.retojulio2026.talent.area.dto.AreaMapper;
import ucu.retojulio2026.talent.area.dto.AreaResponse;

import java.util.List;

@RestController
@RequestMapping("/area")
@Tag(name = "Areas", description = "Alta, consulta, edición y baja de áreas")
public class AreaController {

    private final AreaService areaService;
    private final AreaMapper areaMapper;

    public AreaController(AreaService areaService, AreaMapper areaMapper) {
        this.areaService = areaService;
        this.areaMapper = areaMapper;
    }

    // ===== CREATE =====

    @Operation(summary = "Crear un area")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Area creada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "404", description = "El area padre no existe")
    })
    @PostMapping
    public ResponseEntity<AreaResponse> create(@Valid @RequestBody CreateAreaRequest request) {
        Area created = areaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(areaMapper.toResponse(created));
    }

    // ===== READ =====

    @Operation(summary = "Obtener todas las areas")
    @ApiResponse(responseCode = "200", description = "Areas encontradas")
    @GetMapping
    public ResponseEntity<List<AreaResponse>> getAll() {
        List<AreaResponse> response = areaService.getAll()
                .stream()
                .map(areaMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener un area por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Area encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "404", description = "No existe un area con ese id")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AreaResponse> getById(
            @Parameter(description = "Id del area") @PathVariable String id) {
        Area area = areaService.getById(id);
        return ResponseEntity.ok(areaMapper.toResponse(area));
    }

    // ===== UPDATE =====

    @Operation(summary = "Actualizar un area por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Area actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "404", description = "No existe un area con ese id")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AreaResponse> update(
            @Parameter(description = "Id del area") @PathVariable String id,
            @Valid @RequestBody UpdateAreaRequest request) {
        Area updated = areaService.update(id, request);
        return ResponseEntity.ok(areaMapper.toResponse(updated));
    }

    // ===== DELETE =====

    @Operation(summary = "Eliminar un area por id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Area eliminada (sin contenido)"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "404", description = "No existe un area con ese id")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id del area") @PathVariable String id) {
        areaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
