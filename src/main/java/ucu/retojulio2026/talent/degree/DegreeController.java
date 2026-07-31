package ucu.retojulio2026.talent.degree;

import java.util.List;

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

import ucu.retojulio2026.talent.degree.dto.CreateDegreeRequest;
import ucu.retojulio2026.talent.degree.dto.UpdateDegreeRequest;
import ucu.retojulio2026.talent.degree.dto.DegreeMapper;
import ucu.retojulio2026.talent.degree.dto.DegreeResponse;

@RestController

@RequestMapping("/degree")
@Tag(name = "Carreras", description = "Alta, consulta, actualizacion y baja de carreras")
public class DegreeController {

    private final DegreeService degreeService;
    private final DegreeMapper degreeMapper;

    public DegreeController(DegreeService degreeService, DegreeMapper degreeMapper) {
        this.degreeService = degreeService;
        this.degreeMapper = degreeMapper;
    }

    @Operation(summary = "Crear una carrera")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Carrera creada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ver el detalle por campo)"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
    })
    @PostMapping
    public ResponseEntity<DegreeResponse> create(@Valid @RequestBody CreateDegreeRequest request) {
        Degree created = degreeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(degreeMapper.toResponse(created));

    }

    @Operation(summary = "Obtener una carrera por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrera encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "404", description = "No existe una carrera con ese id")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DegreeResponse> getById(
            @Parameter(description = "Id de la carrera") @PathVariable String id) {
        Degree degree = degreeService.getById(id);
        return ResponseEntity.ok(degreeMapper.toResponse(degree));

    }

    @Operation(summary = "Listar todas las carreras")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
    })
    @GetMapping
    public ResponseEntity<List<DegreeResponse>> getAll() {
        List<DegreeResponse> response = degreeService.getAll()
                .stream()
                .map(degreeMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar carreras por areaId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "400", description = "El areaId es invalido"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
    })
    @GetMapping(params = "areaId")
    public ResponseEntity<List<DegreeResponse>> getByAreaId(
            @Parameter(description = "Id del area", example = "V1StGXR8_Z5j")
            @RequestParam
            @NotBlank(message = "El areaId es obligatorio")
            String areaId) {
        List<DegreeResponse> response = degreeService.getByAreaId(areaId)
                .stream()
                .map(degreeMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Actualizar una carrera por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrera actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ver el detalle por campo)"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "404", description = "No existe una carrera con ese id")
    })
    @PutMapping("/{id}")
    public ResponseEntity<DegreeResponse> update(
            @Parameter(description = "Id de la carrera") @PathVariable String id,
            @Valid @RequestBody UpdateDegreeRequest request) {
        Degree updated = degreeService.update(id, request);
        return ResponseEntity.ok(degreeMapper.toResponse(updated));
    }

    @Operation(summary = "Eliminar una carrera por id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Carrera eliminada (sin contenido)"),
            @ApiResponse(responseCode = "401", description = "No autenticado (sin cookie o token invalido/vencido)"),
            @ApiResponse(responseCode = "404", description = "No existe una carrera con ese id")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id de la carrera") @PathVariable String id) {
        degreeService.delete(id);
        return ResponseEntity.noContent().build();

    }
}
