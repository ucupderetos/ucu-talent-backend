package ucu.retojulio2026.talent.vacancyapplication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationStatus;

public record UpdateVacancyApplicationRequest(

        @Schema(description = "Nuevo estado de la postulación", example = "VISTO")
        @NotNull(message = "El status es obligatorio")
        VacancyApplicationStatus status

) {}
