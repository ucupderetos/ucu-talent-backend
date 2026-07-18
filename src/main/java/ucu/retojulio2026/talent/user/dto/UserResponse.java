package ucu.retojulio2026.talent.user.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import ucu.retojulio2026.talent.common.DocumentType;
import ucu.retojulio2026.talent.user.Role;

//Datos que la API DEVUELVE de un usuario.

public record UserResponse(
        @Schema(description = "Id del usuario (NanoID)", example = "V1StGXR8_Z5j")
        String userId,

        @Schema(description = "Nombre del usuario", example = "Nicolas")
        String name,

        @Schema(description = "Apellido del usuario", example = "Gonzalez")
        String surname,

        @Schema(description = "Email del usuario", example = "nicogon@ucu.edu.uy")
        String email,

        @Schema(description = "Rol del usuario", example = "ALUMNO")
        Role role,

        @Schema(description = "Numero de telefono", example = "+59899123456")
        String phoneNumber,

        @Schema(description = "Tipo de documento", example = "CEDULA_IDENTIDAD")
        DocumentType documentType,

        @Schema(description = "Numero de documento", example = "1.234.567-8")
        String documentNumber,

        @Schema(description = "URL de LinkedIn", example = "https://linkedin.com/in/nicolas-gonzalez")
        String linkedinUrl,

        @Schema(description = "Fecha de alta del usuario", example = "2026-07-15")
        LocalDate registeredAt
) {}
