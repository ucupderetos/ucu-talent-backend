package ucu.retojulio2026.talent.workexperience.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;


public record CreateWorkExperienceRequest(

        @Schema(description = "Id del perfil de alumno (NanoID de 12 caracteres)", example = "V1StGXR8_Z5j")
        @NotBlank(message = "El studentProfileId es obligatorio")
        String studentProfileId,

        @Schema(description = "Empresa donde se desempeño", example = "Acme S.A.")
        String company,

        @Schema(description = "Cargo o posicion", example = "Backend Developer")
        String position,

        @Schema(description = "Fecha de inicio", example = "2022-01-01")
        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate startDate,

        @Schema(description = "Fecha de fin (null si es el trabajo actual)", example = "2024-01-01")
        LocalDate endDate,

        @Schema(description = "Descripcion de las tareas", example = "Desarrollo de APIs REST con Spring Boot")
        String description
) {
}
