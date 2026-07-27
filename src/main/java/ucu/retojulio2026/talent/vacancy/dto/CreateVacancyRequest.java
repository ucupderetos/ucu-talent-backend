package ucu.retojulio2026.talent.vacancy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ucu.retojulio2026.talent.common.Department;
import ucu.retojulio2026.talent.vacancy.ContractType;
import ucu.retojulio2026.talent.vacancy.VacancyStatus;
import ucu.retojulio2026.talent.vacancy.Modality;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CreateVacancyRequest (

    @Schema(description = "La empresa para el puesto")
    @NotBlank(message = "La empresa es obligatoria")
    String companyId,

    @Schema(description = "El Area para el puesto")
    @NotBlank(message = "El area es obligatoria")
    String areaId,

    @Schema(description = "Fecha de publicación del puesto", example = "2026-08-15")
    @NotNull(message = "La fecha de publicación es obligatoria")
    LocalDate publicationDate,

    @Schema(description = "Fecha de cierre del puesto", example = "2026-09-15")
    @NotNull(message = "La fecha de cierre es obligatoria")
    LocalDate closingDate,

    @Schema(description = "Localidad del puesto", example = "MONTEVIDEO")
    @NotNull(message = "La localidad es obligatoria")
    Department location,

    @Schema(description = "Modalidad de trabajo", example = "REMOTO")
    @NotNull(message = "La modalidad es obligatoria")
    Modality modality,

    @Schema(description = "Nombre del puesto", example = "Java Backend Developer")
    @NotBlank(message = "El nombre es obligatorio")
    String name,

    @Schema(description = "Descripción del puesto", example = "Desarrollo de APIs REST con Spring Boot")
    @NotBlank(message = "La descripción es obligatoria")
    String description,

    @Schema(description = "Requisitos del puesto", example = "Java 21, Spring Boot, PostgreSQL")
    @NotBlank(message = "Los requisitos son obligatorios")
    String requirements,

    @Schema(description = "Tipo de contrato", example = "FULL_TIME")
    @NotNull(message = "El tipo de contrato es obligatorio")
    ContractType contractType,

    @Schema(description = "Salario", example = "USD 700")
    @NotBlank(message = "El salario es obligatorio")
    String salary
) {}
