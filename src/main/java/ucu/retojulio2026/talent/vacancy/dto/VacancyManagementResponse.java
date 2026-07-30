package ucu.retojulio2026.talent.vacancy.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record VacancyManagementResponse(

        @Schema(description = "Datos del puesto")
        VacancyResponse vacancy,

        @Schema(description = "Nombre de la empresa dueña del puesto", example = "Acme S.A.")
        String companyName,

        @Schema(description = "Nombre del area del puesto", example = "Desarrollo de Software")
        String areaName,

        @Schema(description = "Cantidad total de postulaciones al puesto", example = "12")
        long applicationCount,

        @Schema(description = "Cantidad de postulaciones nuevas al puesto (VacancyApplicationStatus = PENDIENTE)", example = "3")
        long newApplicationsCount
) {}
