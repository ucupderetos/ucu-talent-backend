package ucu.retojulio2026.talent.vacancy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ucu.retojulio2026.talent.vacancy.Departamento;
import ucu.retojulio2026.talent.vacancy.VacancyStatus;
import ucu.retojulio2026.talent.vacancy.Modality;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record VacancyResponse (
        @Schema(description = "Id del puesto (NanoID)", example = "V1StGXR8_Z5j")
        String vacancyId,

        @Schema(description = "Id de la compañia (NanoID)", example = "V1StGXR8_Z5j")
        String companyId,

        @Schema(description = "Id del area (NanoID)", example = "V1StGXR8_Z5j")
        String areaId,

        @Schema(description = "Fecha de publicación", example = "2026-07-15")
        LocalDate publicationDate,

        @Schema(description = "Fecha de cierre del puesto", example = "2026-08-15")
        LocalDate closingDate,

        @Schema(description = "Fecha de creación", example = "2026-07-16T14:30:15")
        LocalDateTime createdAt,

        @Schema(description = "Fecha de revisión", example = "2026-07-16T16:45:00")
        LocalDateTime reviewedAt,

        @Schema(description = "Comentario del administrador", example = "Comentario o null")
        String adminComment,

        @Schema(description = "Localidad del puesto", example = "MONTEVIDEO")
        Departamento location,

        @Schema(description = "Modalidad de trabajo", example = "REMOTO")
        Modality modality,

        @Schema(description = "Estado del puesto", example = "PENDIENTE")
        VacancyStatus status,

        @Schema(description = "Nombre del puesto", example = "Java Backend Developer")
        String name,

        @Schema(description = "Descripción del puesto", example = "Desarrollo de APIs REST con Spring Boot")
        String description,

        @Schema(description = "Requisitos del puesto", example = "Java 21, Spring Boot, PostgreSQL")
        String requirements,

        @Schema(description = "Tipo de contrato", example = "Full time")
        String contractType,

        @Schema(description = "Rango salarial", example = "USD 700 - 2000")
        String salaryRange
        ) {}
