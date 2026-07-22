package ucu.retojulio2026.talent.education.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ucu.retojulio2026.talent.education.Education.DegreeLevel;

import java.time.LocalDate;


public record CreateEducationRequest(

        @Schema(description = "Id del perfil de alumno (NanoID de 12 caracteres)", example = "V1StGXR8_Z5j")
        @NotBlank(message = "El studentProfileId es obligatorio")
        String studentProfileId,

        @Schema(description = "Nivel del titulo", example = "GRADO")
        @NotNull(message = "El titulo es obligatorio")
        DegreeLevel degreeLevel,

        @Schema(description = "Id de la carrera (NanoID de 12 caracteres)", example = "V1StGXR8_Z5j")
        @NotBlank(message = "La carrera es obligatoria")
        String degreeId,

        @Schema(description = "Institucion (obligatoria si la carrera no es UCU)", example = "Universidad ORT Uruguay")
        String institution,

        @Schema(description = "Descripcion", example = "Cursada completa, tesis en curso")
        String description,

        @Schema(description = "Fecha de inicio", example = "2020-03-01")
        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate startDate,

        @Schema(description = "Fecha de fin (null si esta en curso)", example = "2024-12-01")
        LocalDate endDate
) {
}
