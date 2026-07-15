package ucu.retojulio2026.talent.workexperience;

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
@RequestMapping("/work-experience")
@Validated
@Tag(name = "Experiencia laboral", description = "Gestion de experiencia laboral de alumnos")
public class WorkExperienceController {

    private final WorkExperienceService workExperienceService;

    public WorkExperienceController(WorkExperienceService workExperienceService) {
        this.workExperienceService = workExperienceService;
    }

    @Operation(summary = "Crear una experiencia laboral")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Experiencia creada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos")
    })
    @PostMapping
    public ResponseEntity<WorkExperience> create(@Valid @RequestBody WorkExperience workExperience) {
        WorkExperience created = workExperienceService.create(workExperience);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Obtener una experiencia laboral por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Experiencia encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe una experiencia con ese id")
    })
    @GetMapping("/{id}")
    public ResponseEntity<WorkExperience> getById(
            @Parameter(description = "Id de workExperience") @PathVariable String id) {
        return ResponseEntity.ok(workExperienceService.getById(id));
    }

    @Operation(summary = "Listar experiencia laboral por perfilAlumnoId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "400", description = "Parametro invalido")
    })
    @GetMapping(params = "perfilAlumnoId")
    public ResponseEntity<List<WorkExperience>> getByPerfilAlumnoId(
            @Parameter(description = "Id del perfil alumno")
            @RequestParam
            @NotBlank(message = "perfilAlumnoId es obligatorio")
            String perfilAlumnoId) {
        return ResponseEntity.ok(workExperienceService.getByPerfilAlumnoId(perfilAlumnoId));
    }

    @Operation(summary = "Actualizar una experiencia laboral por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Experiencia actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @ApiResponse(responseCode = "404", description = "No existe una experiencia con ese id")
    })
    @PutMapping("/{id}")
    public ResponseEntity<WorkExperience> update(
            @Parameter(description = "Id de workExperience") @PathVariable String id,
            @Valid @RequestBody WorkExperience workExperience) {
        return ResponseEntity.ok(workExperienceService.update(id, workExperience));
    }

    @Operation(summary = "Eliminar una experiencia laboral por id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Experiencia eliminada"),
            @ApiResponse(responseCode = "404", description = "No existe una experiencia con ese id")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id de workExperience") @PathVariable String id) {
        workExperienceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
