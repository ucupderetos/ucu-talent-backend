package ucu.retojulio2026.talent.vacancy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ucu.retojulio2026.talent.company.dto.CompanyPublicResponse;

public record ResolvedVacancyResponse(

        @Schema(description = "Datos del puesto")
        VacancyResponse vacancy,

        @Schema(description = "Datos publicos de la empresa dueña del puesto")
        CompanyPublicResponse company,

        @Schema(description = "Nombre del area del puesto", example = "Desarrollo de Software")
        String areaName,

        @Schema(description = "Nombre del area padre. Null si el area es de primer nivel", example = "Tecnologia")
        String parentAreaName
) {}
