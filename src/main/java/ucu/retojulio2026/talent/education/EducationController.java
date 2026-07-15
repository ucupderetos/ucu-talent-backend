package ucu.retojulio2026.talent.education;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/education")
@Validated
@Tag(name = "Educacion", description = "Gestion de educacion de alumnos")
public class EducationController {

    private final EducationService educationService;

    public EducationController(EducationService educationService) {
        this.educationService = educationService;
    }

    @Operation(summary = "Crear un registro de educacion")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registro creado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos")
    })
    @PostMapping
    public ResponseEntity<Education> create(@Valid @RequestBody Education education) {
        Education created = educationService.create(education);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Obtener un registro de educacion por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese id")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Education> getById(
            @Parameter(description = "Id de education") @PathVariable String id) {
        return ResponseEntity.ok(educationService.getById(id));
    }

    @Operation(summary = "Listar educacion por perfilAlumnoId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "400", description = "Parametro invalido")
    })
    @GetMapping(params = "perfilAlumnoId")
    public ResponseEntity<List<Education>> getByPerfilAlumnoId(
            @Parameter(description = "Id del perfil alumno")
            @RequestParam
            @NotBlank(message = "perfilAlumnoId es obligatorio")
            String perfilAlumnoId) {
        return ResponseEntity.ok(educationService.getByPerfilAlumnoId(perfilAlumnoId));
    }

    @Operation(summary = "Actualizar un registro de educacion por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese id")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Education> update(
            @Parameter(description = "Id de education") @PathVariable String id,
            @Valid @RequestBody Education education) {
        return ResponseEntity.ok(educationService.update(id, education));
    }

    @Operation(summary = "Eliminar un registro de educacion por id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Registro eliminado"),
            @ApiResponse(responseCode = "404", description = "No existe un registro con ese id")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id de education") @PathVariable String id) {
        educationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
