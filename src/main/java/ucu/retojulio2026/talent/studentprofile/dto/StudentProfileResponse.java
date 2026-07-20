package ucu.retojulio2026.talent.studentprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import ucu.retojulio2026.talent.common.DocumentType;

import java.util.List;

public record StudentProfileResponse(
        @Schema(description = "Id del perfil de alumno (NanoID). Es el mismo valor que el userId.", example = "V1StGXR8_Z5j")
        String studentProfileId,

        @Schema(description = "Nombre del alumno", example = "Nicolas")
        String name,

        @Schema(description = "Apellido del alumno", example = "Gonzalez")
        String surname,

        @Schema(description = "Tipo de documento", example = "CEDULA_IDENTIDAD")
        DocumentType documentType,

        @Schema(description = "Numero de documento", example = "1.234.567-8")
        String documentNumber,

        @Schema(description = "Numero de telefono", example = "+59899123456")
        String phoneNumber,

        @Schema(description = "URL de LinkedIn", example = "https://linkedin.com/in/nicolas-gonzalez")
        String linkedinUrl,

        @Schema(description = "Skills del alumno", example = "[\"Java\", \"Spring Boot\"]")
        List<String> skills
) {}
