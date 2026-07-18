package ucu.retojulio2026.talent.vacancy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ucu.retojulio2026.talent.vacancy.Departamento;
import ucu.retojulio2026.talent.vacancy.VacancyStatus;
import ucu.retojulio2026.talent.vacancy.Modality;

import java.time.LocalDate;

public record CreateVacancyRequest (

    @NotBlank(message = "La empresa es obligatoria")
    String companyId,

    @NotBlank(message = "El área es obligatoria")
    String areaId,

    @Schema(description = "Fecha de publicación del puesto", example = "2026-08-15")
    LocalDate publicationDate,

    @Schema(description = "Fecha de cierre del puesto", example = "2026-08-15")
    LocalDate closingDate,

    @Schema(description = "Localidad del puesto", example = "MONTEVIDEO")
    @NotNull(message = "La localidad es obligatoria")
    Departamento location,

    @Schema(description = "Modalidad de trabajo", example = "REMOTO")
    @NotNull(message = "La modalidad es obligatoria")
    Modality modality,

    @Schema(description = "Estado del puesto", example = "PENDIENTE")
    VacancyStatus status,

    @Schema(description = "Nombre del puesto", example = "Java Backend Developer")
    @NotBlank(message = "El nombre es obligatorio")
    String name,

    @Schema(description = "Descripción del puesto", example = "Desarrollo de APIs REST con Spring Boot")
    @NotBlank(message = "La descripción es obligatoria")
    String description,

    @Schema(description = "Requisitos del puesto", example = "Java 21, Spring Boot, PostgreSQL")
    @NotBlank(message = "Los requisitos son obligatorios")
    String requirements,

    @Schema(description = "Tipo de contrato", example = "Full time")
    @NotBlank(message = "El tipo de contrato es obligatorio")
    String contractType,

    @Schema(description = "Rango salarial", example = "USD 800 - 2000")
    @NotBlank(message = "El rango salarial es obligatorio")
    String salaryRange
) {}
