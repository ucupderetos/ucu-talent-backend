package ucu.retojulio2026.talent.education.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ucu.retojulio2026.talent.education.Education.DegreeLevel;

import java.time.LocalDate;

public record EducationResponse(

        @Schema(description = "Id del registro de educacion (NanoID)", example = "AbC123xYz890")
        String educationId,

        @Schema(description = "Id del perfil de alumno", example = "V1StGXR8_Z5j")
        String studentProfileId,

        @Schema(description = "Nivel del titulo", example = "GRADO")
        DegreeLevel degreeLevel,

        @Schema(description = "Id de la carrera", example = "V1StGXR8_Z5j")
        String degreeId,

        @Schema(description = "Institucion", example = "Universidad UCU Uruguay")
        String institution,

        @Schema(description = "Descripcion", example = "Cursada completa, tesis en curso")
        String description,

        @Schema(description = "Fecha de inicio", example = "2020-03-01")
        LocalDate startDate,

        @Schema(description = "Fecha de fin (null si esta en curso)", example = "2024-12-01")
        LocalDate endDate
) {
}
