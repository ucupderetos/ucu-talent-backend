package ucu.retojulio2026.talent.vacancyapplication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationStatus;

import java.time.LocalDate;

public record CreateVacancyApplicationRequest(

        @Schema(description = "Id de la vacante (NanoID de 12 caracteres)", example = "V1StGXR8_Z5j")
        @NotBlank(message = "El vacancyId es obligatorio")
        String vacancyId,

        @Schema(description = "Id del perfil de alumno (NanoID de 12 caracteres)", example = "V1StGXR8_Z5j")
        @NotBlank(message = "El studentProfileId es obligatorio")
        String studentProfileId,

        @Schema(description = "Estado de la postulación", example = "PENDIENTE")
        VacancyApplicationStatus status,

        @Schema(description = "Fecha de postulación", example = "2026-07-17")
        LocalDate appliedAt
) {}
