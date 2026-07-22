package ucu.retojulio2026.talent.vacancyapplication.dto;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationStatus;

public record VacancyApplicationStatusSummaryResponse(
        @Schema(description = "Total de postulaciones", example = "50")
        long total,

        @Schema(description = "Postulaciones pendientes", example = "20")
        long pendiente,

        @Schema(description = "Postulaciones vistas", example = "25")
        long visto,

        @Schema(description = "Postulaciones finalizadas", example = "5")
        long finalizado
) {
    public static VacancyApplicationStatusSummaryResponse from(Map<VacancyApplicationStatus, Long> counts) {
        long pendiente = counts.getOrDefault(VacancyApplicationStatus.PENDIENTE, 0L);
        long visto = counts.getOrDefault(VacancyApplicationStatus.VISTO, 0L);
        long finalizado = counts.getOrDefault(VacancyApplicationStatus.FINALIZADO, 0L);
        return new VacancyApplicationStatusSummaryResponse(pendiente + visto + finalizado, pendiente, visto, finalizado);
    }
}
