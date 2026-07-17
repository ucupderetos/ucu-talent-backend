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

//Bean que indica que es un Controller cuyos returns se serializan directo a JSON
@RestController
// Da el path de la url a donde llamar al Endpoint, ej: localhost:8080/degree/{id} para obtener una carrera
@RequestMapping("/degree")
@Tag(name = "Carreras", description = "Alta, consulta, actualizacion y baja de carreras") // agrupa los endpoints en Swagger UI
public class DegreeController {

    private final DegreeService degreeService;
    private final DegreeMapper degreeMapper;

    public DegreeController(DegreeService degreeService, DegreeMapper degreeMapper) {
        this.degreeService = degreeService;
        this.degreeMapper = degreeMapper;
    }

    //GetMapping indica que usa el verbo HTTP GET para obtener un recurso
    @Operation(summary = "Obtener una carrera por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrera encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe una carrera con ese id")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DegreeResponse> getById(
            @Parameter(description = "Id de la carrera (NanoID de 12 caracteres)") @PathVariable String id) {
        Degree degree = degreeService.getById(id);
        return ResponseEntity.ok(degreeMapper.toResponse(degree));

        //Devuelve un DegreeResponse (Json) mas codigo HTTP 200 (OK)
        //Si no existe lanza 404 NotFound en GlobalExceptionHandler
    }

    @Operation(summary = "Listar todas las carreras")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido")
    })
    @GetMapping
    public ResponseEntity<List<DegreeResponse>> getAll() {
        List<DegreeResponse> response = degreeService.getAll()
                .stream()
                .map(degreeMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar una carrera por nombre")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrera encontrada"),
            @ApiResponse(responseCode = "400", description = "El nombre es invalido"),
            @ApiResponse(responseCode = "404", description = "No existe una carrera con ese nombre")
    })
    @GetMapping(params = "name")
    public ResponseEntity<DegreeResponse> getByName(
            @Parameter(description = "Nombre exacto de la carrera", example = "Licenciatura en Informatica")
            @RequestParam
            @NotBlank(message = "El nombre es obligatorio")
            String name) {
        Degree degree = degreeService.getByName(name);
        return ResponseEntity.ok(degreeMapper.toResponse(degree));
    }

    @Operation(summary = "Listar carreras por areaId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "400", description = "El areaId es invalido")
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

    //PostMapping indica que usa el verbo HTTP POST para guardar un recurso.
    //@Valid dispara las validaciones del CreateDegreeRequest (@NotBlank, @NotNull, etc)
    @Operation(summary = "Crear una carrera")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Carrera creada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ver el detalle por campo)")
    })
    @PostMapping
    public ResponseEntity<DegreeResponse> create(@Valid @RequestBody CreateDegreeRequest request) {
        Degree created = degreeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(degreeMapper.toResponse(created));

        //Devuelve el DegreeResponse mas codigo HTTP 201 (Created)
    }

    //PutMapping indica que usa el verbo HTTP PUT para actualizar un recurso existente.
    @Operation(summary = "Actualizar una carrera por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrera actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (ver el detalle por campo)"),
            @ApiResponse(responseCode = "404", description = "No existe una carrera con ese id")
    })
    @PutMapping("/{id}")
    public ResponseEntity<DegreeResponse> update(
            @Parameter(description = "Id de la carrera (NanoID de 12 caracteres)") @PathVariable String id,
            @Valid @RequestBody UpdateDegreeRequest request) {
        Degree updated = degreeService.update(id, request);
        return ResponseEntity.ok(degreeMapper.toResponse(updated));
    }

    //Lo mismo que los otros con DELETE
    @Operation(summary = "Eliminar una carrera por id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Carrera eliminada (sin contenido)"),
            @ApiResponse(responseCode = "404", description = "No existe una carrera con ese id")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id de la carrera (NanoID de 12 caracteres)") @PathVariable String id) {
        degreeService.delete(id);
        return ResponseEntity.noContent().build();

        //devuelve solo el codigo 204 No Content - No siempre hay que devolver un JSON al front
        //pero siempre un codigo HTTP para que sepan si salio bien (200s) o hubo algun fallo
        // (400s cliente) (500s servidor)
    }
}