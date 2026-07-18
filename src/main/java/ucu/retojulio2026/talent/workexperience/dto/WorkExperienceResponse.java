package ucu.retojulio2026.talent.workexperience.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;


public record WorkExperienceResponse(

        @Schema(description = "Id de la experiencia laboral (NanoID)", example = "AbC123xYz890")
        String workExperienceId,

        @Schema(description = "Id del perfil de alumno", example = "V1StGXR8_Z5j")
        String studentProfileId,

        @Schema(description = "Empresa donde se desempeño", example = "Acme S.A.")
        String company,

        @Schema(description = "Cargo o posicion", example = "Backend Developer")
        String position,

        @Schema(description = "Fecha de inicio", example = "2022-01-01")
        LocalDate startDate,

        @Schema(description = "Fecha de fin (null si es el trabajo actual)", example = "2024-01-01")
        LocalDate endDate,

        @Schema(description = "Descripcion de las tareas", example = "Desarrollo de APIs REST con Spring Boot")
        String description
) {
}
