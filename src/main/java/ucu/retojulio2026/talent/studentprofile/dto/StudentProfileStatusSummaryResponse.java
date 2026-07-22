package ucu.retojulio2026.talent.studentprofile.dto;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

import ucu.retojulio2026.talent.user.AccountStatus;

public record StudentProfileStatusSummaryResponse(
        @Schema(description = "Total de alumnos", example = "40")
        long total,

        @Schema(description = "Alumnos pendientes de aprobacion", example = "5")
        long pendiente,

        @Schema(description = "Alumnos aprobados", example = "34")
        long aprobado,

        @Schema(description = "Alumnos rechazados", example = "1")
        long rechazado
) {
    public static StudentProfileStatusSummaryResponse from(Map<AccountStatus, Long> counts) {
        long pendiente = counts.getOrDefault(AccountStatus.PENDIENTE, 0L);
        long aprobado = counts.getOrDefault(AccountStatus.APROBADO, 0L);
        long rechazado = counts.getOrDefault(AccountStatus.RECHAZADO, 0L);
        return new StudentProfileStatusSummaryResponse(pendiente + aprobado + rechazado, pendiente, aprobado, rechazado);
    }
}
