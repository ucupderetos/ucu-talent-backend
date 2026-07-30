package ucu.retojulio2026.talent.vacancyapplication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ucu.retojulio2026.talent.vacancy.VacancyStatus;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationStatus;

import java.time.LocalDate;


public record VacancyApplicationStudentResponse(

        @Schema(description = "Id de la postulación", example = "V1StGXR8_Z5j")
        String vacancyApplicationId,

        @Schema(description = "Id de la vacante", example = "V1StGXR8_Z5j")
        String vacancyId,

        @Schema(description = "Nombre de la vacante", example = "Desarrollador Java Semi Senior")
        String vacancyName,

        @Schema(description = "Id de la empresa dueña de la vacante", example = "V1StGXR8_Z5j")
        String companyId,

        @Schema(description = "Nombre de la empresa dueña de la vacante", example = "Acme S.A.")
        String companyName,

        @Schema(description = "Fecha de postulación", example = "2026-07-17")
        LocalDate appliedAt,

        @Schema(description = "Estado de la postulación", example = "PENDIENTE")
        VacancyApplicationStatus status,

        @Schema(description = "Estado de la vacante a la que se postuló", example = "PUBLICADO")
        VacancyStatus vacancyStatus
) {}
