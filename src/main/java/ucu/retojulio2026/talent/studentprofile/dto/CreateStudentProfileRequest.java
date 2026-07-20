package ucu.retojulio2026.talent.studentprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ucu.retojulio2026.talent.common.DocumentType;

import java.util.List;

public record CreateStudentProfileRequest(

        @Schema(description = "Id del usuario dueño del perfil (NanoID de 12 caracteres)", example = "V1StGXR8_Z5j")
        @NotBlank(message = "El userId es obligatorio")
        String userId,

        @Schema(description = "Nombre del alumno", example = "Nicolas")
        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @Schema(description = "Apellido del alumno", example = "Gonzalez")
        @NotBlank(message = "El apellido es obligatorio")
        String surname,

        @Schema(description = "Tipo de documento", example = "CEDULA_IDENTIDAD")
        @NotNull(message = "El tipo de documento es obligatorio")
        DocumentType documentType,

        @Schema(description = "Numero de documento", example = "1.234.567-8")
        @NotBlank(message = "El numero de documento es obligatorio")
        String documentNumber,

        @Schema(description = "Numero de telefono (opcional)", example = "+59899123456")
        String phoneNumber,

        @Schema(description = "URL de LinkedIn (opcional)", example = "https://linkedin.com/in/nicolas-gonzalez")
        String linkedinUrl,

        @Schema(description = "Skills del alumno, cargadas desde el frontend", example = "[\"Java\", \"Spring Boot\", \"SQL\"]")
        List<String> skills

) {}
