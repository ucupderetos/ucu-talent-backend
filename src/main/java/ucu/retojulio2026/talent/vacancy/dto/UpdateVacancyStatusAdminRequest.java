package ucu.retojulio2026.talent.vacancy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ucu.retojulio2026.talent.vacancy.VacancyStatus;

public record UpdateVacancyStatusAdminRequest(
        @Schema(description = "El comentario del administrador, en caso que sea necesario")
        String adminComment,
        @Schema(description = "El estado del Puesto", example = "PUBLICADO")
        @NotNull(message = "El nuevo estado es obligatorio")
        VacancyStatus status
) {
}
