package ucu.retojulio2026.talent.Vacancy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ucu.retojulio2026.talent.Vacancy.Departamento;
import ucu.retojulio2026.talent.Vacancy.JobStatus;
import ucu.retojulio2026.talent.Vacancy.Modality;

import java.time.LocalDate;

public record CreateVacancyRequest (
    @Schema(description = "Fecha de cierre de la vacante", example = "2026-08-15")
    LocalDate closingDate,

    @Schema(description = "Localidad de la vacante", example = "MONTEVIDEO")
    @NotNull(message = "La localidad es obligatoria")
    Departamento locality,

    @Schema(description = "Modalidad de trabajo", example = "REMOTE")
    @NotNull(message = "La modalidad es obligatoria")
    Modality modality,

    @Schema(description = "Estado de la vacante", example = "PENDING")
    JobStatus status,

    @Schema(description = "Nombre de la vacante", example = "Java Backend Developer")
    @NotBlank(message = "El nombre es obligatorio")
    String name,

    @Schema(description = "Descripción de la vacante", example = "Desarrollo de APIs REST con Spring Boot")
    @NotBlank(message = "La descripción es obligatoria")
    String description,

    @Schema(description = "Requisitos de la vacante", example = "Java 21, Spring Boot, PostgreSQL")
    @NotBlank(message = "Los requisitos son obligatorios")
    String requirements,

    @Schema(description = "Tipo de contrato", example = "Full time")
    @NotBlank(message = "El tipo de contrato es obligatorio")
    String contractType,

    @Schema(description = "Rango salarial", example = "USD 800 - 2000")
    @NotBlank(message = "El rango salarial es obligatorio")
    String salaryRange
) {}
