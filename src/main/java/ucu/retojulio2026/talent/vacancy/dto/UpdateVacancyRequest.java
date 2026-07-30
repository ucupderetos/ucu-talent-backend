package ucu.retojulio2026.talent.vacancy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ucu.retojulio2026.talent.common.Department;
import ucu.retojulio2026.talent.vacancy.ContractType;
import ucu.retojulio2026.talent.vacancy.Modality;

import java.time.LocalDate;

public record UpdateVacancyRequest(

    @Schema(description = "Fecha de publicación del puesto", example = "2026-08-15")
    LocalDate publicationDate,

    @Schema(description = "Fecha de cierre del puesto", example = "2026-09-15")
    LocalDate closingDate,

    @Schema(description = "Localidad del puesto", example = "MONTEVIDEO")
    Department location,

    @Schema(description = "Modalidad de trabajo", example = "REMOTO")
    Modality modality,

    @Schema(description = "Nombre del puesto", example = "Java Backend Developer")
    String name,

    @Schema(description = "Descripción del puesto", example = "Desarrollo de APIs REST con Spring Boot")
    String description,

    @Schema(description = "Requisitos del puesto", example = "Java 21, Spring Boot, PostgreSQL")
    String requirements,

    @Schema(description = "Tipo de contrato", example = "Full time")
    ContractType contractType,

    @Schema(description = "Salario", example = "USD 700")
    String salary
) {}
