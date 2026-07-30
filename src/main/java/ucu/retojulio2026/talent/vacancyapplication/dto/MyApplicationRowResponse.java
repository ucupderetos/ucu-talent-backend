package ucu.retojulio2026.talent.vacancyapplication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ucu.retojulio2026.talent.vacancy.dto.VacancyResponse;

public record MyApplicationRowResponse(

        @Schema(description = "Datos de la postulación del alumno")
        VacancyApplicationStudentResponse application,

        @Schema(description = "Datos del puesto al que se postuló")
        VacancyResponse vacancy,

        @Schema(description = "Nombre de la empresa dueña del puesto", example = "Acme S.A.")
        String companyName,

        @Schema(description = "Nombre del area del puesto", example = "Desarrollo de Software")
        String areaName
) {}
