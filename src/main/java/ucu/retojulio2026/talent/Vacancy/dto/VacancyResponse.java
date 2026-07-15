package ucu.retojulio2026.talent.Vacancy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ucu.retojulio2026.talent.Vacancy.Departamento;
import ucu.retojulio2026.talent.Vacancy.JobStatus;
import ucu.retojulio2026.talent.Vacancy.Modality;

import java.time.LocalDate;

public record VacancyResponse (
        @Schema(description = "Id de la vacante (NanoID)", example = "V1StGXR8_Z5j")
        String vacancyId,

        @Schema(description = "Fecha de publicación", example = "2026-07-15")
        LocalDate publicationDate,

        @Schema(description = "Fecha de cierre de la vacante", example = "2026-08-15")
        LocalDate closingDate,

        @Schema(description = "Localidad de la vacante", example = "MONTEVIDEO")
        Departamento locality,

        @Schema(description = "Modalidad de trabajo", example = "REMOTE")
        Modality modality,

        @Schema(description = "Estado de la vacante", example = "PENDING")
        JobStatus status,

        @Schema(description = "Nombre de la vacante", example = "Java Backend Developer")
        String name,

        @Schema(description = "Descripción de la vacante", example = "Desarrollo de APIs REST con Spring Boot")
        String description,

        @Schema(description = "Requisitos de la vacante", example = "Java 21, Spring Boot, PostgreSQL")
        String requirements,

        @Schema(description = "Tipo de contrato", example = "Full time")
        String contractType,

        @Schema(description = "Rango salarial", example = "USD 700 - 2000")
        String salaryRange
        ) {}
