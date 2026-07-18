package ucu.retojulio2026.talent.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import ucu.retojulio2026.talent.common.DocumentType;

//Datos EDITABLES de un usuario (PUT). No incluye email, password ni role (se cambian por flujos aparte).
public record UpdateUserRequest(

        @Schema(description = "Nombre del usuario", example = "Nicolas")
        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @Schema(description = "Apellido del usuario", example = "Gonzalez")
        @NotBlank(message = "El apellido es obligatorio")
        String surname,

        @Schema(description = "Numero de telefono (opcional)", example = "+59899123456")
        String phoneNumber,

        @Schema(description = "Tipo de documento (opcional)", example = "CEDULA_IDENTIDAD")
        DocumentType documentType,

        @Schema(description = "Numero de documento (opcional)", example = "1.234.567-8")
        String documentNumber,

        @Schema(description = "URL de LinkedIn (opcional)", example = "https://linkedin.com/in/nicolas-gonzalez")
        String linkedinUrl

) {}
