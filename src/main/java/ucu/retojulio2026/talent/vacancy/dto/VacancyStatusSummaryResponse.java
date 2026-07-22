package ucu.retojulio2026.talent.vacancy.dto;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

import ucu.retojulio2026.talent.vacancy.VacancyStatus;

public record VacancyStatusSummaryResponse(
        @Schema(description = "Total de puestos", example = "20")
        long total,

        @Schema(description = "Puestos pendientes de revision", example = "2")
        long pendiente,

        @Schema(description = "Puestos publicados", example = "15")
        long publicado,

        @Schema(description = "Puestos finalizados", example = "3")
        long finalizado
) {
    public static VacancyStatusSummaryResponse from(Map<VacancyStatus, Long> counts) {
        long pendiente = counts.getOrDefault(VacancyStatus.PENDIENTE, 0L);
        long publicado = counts.getOrDefault(VacancyStatus.PUBLICADO, 0L);
        long finalizado = counts.getOrDefault(VacancyStatus.FINALIZADO, 0L);
        return new VacancyStatusSummaryResponse(pendiente + publicado + finalizado, pendiente, publicado, finalizado);
    }
}
