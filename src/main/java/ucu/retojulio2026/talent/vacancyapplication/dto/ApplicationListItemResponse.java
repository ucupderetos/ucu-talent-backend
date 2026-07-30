package ucu.retojulio2026.talent.vacancyapplication.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ApplicationListItemResponse(

        @Schema(description = "Datos de la postulación")
        VacancyApplicationResponse application,

        @Schema(description = "Nombre del alumno postulante", example = "Ana")
        String studentName,

        @Schema(description = "Apellido del alumno postulante", example = "Pérez")
        String studentSurname,

        @Schema(description = "Email del alumno postulante", example = "ana.perez@correo.ucu.edu.uy")
        String studentEmail,

        @Schema(description = "Id de la vacante", example = "V1StGXR8_Z5j")
        String vacancyId,

        @Schema(description = "Nombre de la vacante", example = "Desarrollador Java Semi Senior")
        String vacancyName,

        @Schema(description = "Id de la empresa dueña de la vacante", example = "V1StGXR8_Z5j")
        String companyId,

        @Schema(description = "Nombre de la empresa dueña de la vacante", example = "Acme S.A.")
        String companyName
) {}
