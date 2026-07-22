package ucu.retojulio2026.talent.company.dto;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

import ucu.retojulio2026.talent.user.AccountStatus;

public record CompanyStatusSummaryResponse(
        @Schema(description = "Total de empresas", example = "12")
        long total,

        @Schema(description = "Empresas pendientes de aprobacion", example = "3")
        long pendiente,

        @Schema(description = "Empresas aprobadas", example = "8")
        long aprobado,

        @Schema(description = "Empresas rechazadas", example = "1")
        long rechazado
) {
    public static CompanyStatusSummaryResponse from(Map<AccountStatus, Long> counts) {
        long pendiente = counts.getOrDefault(AccountStatus.PENDIENTE, 0L);
        long aprobado = counts.getOrDefault(AccountStatus.APROBADO, 0L);
        long rechazado = counts.getOrDefault(AccountStatus.RECHAZADO, 0L);
        return new CompanyStatusSummaryResponse(pendiente + aprobado + rechazado, pendiente, aprobado, rechazado);
    }
}
