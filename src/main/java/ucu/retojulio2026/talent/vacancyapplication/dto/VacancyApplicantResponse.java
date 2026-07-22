package ucu.retojulio2026.talent.vacancyapplication.dto;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import ucu.retojulio2026.talent.common.DocumentType;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationStatus;

public record VacancyApplicantResponse(
        @Schema(description = "Id de la postulación", example = "V1StGXR8_Z5j")
        String vacancyApplicationId,

        @Schema(description = "Estado de la postulación", example = "PENDIENTE")
        VacancyApplicationStatus status,

        @Schema(description = "Fecha de postulación", example = "2026-07-17")
        LocalDate appliedAt,

        @Schema(description = "Id del perfil de alumno (= userId)", example = "V1StGXR8_Z5j")
        String studentProfileId,

        @Schema(description = "Nombre del alumno", example = "Nicolas")
        String name,

        @Schema(description = "Apellido del alumno", example = "Gonzalez")
        String surname,

        @Schema(description = "Tipo de documento", example = "CEDULA_IDENTIDAD")
        DocumentType documentType,

        @Schema(description = "Numero de documento", example = "1.234.567-8")
        String documentNumber,

        @Schema(description = "Numero de telefono", example = "099123456")
        String phoneNumber,

        @Schema(description = "URL de LinkedIn", example = "https://linkedin.com/in/nicolas-gonzalez")
        String linkedinUrl,

        @Schema(description = "Skills del alumno")
        List<String> skills
) {}
