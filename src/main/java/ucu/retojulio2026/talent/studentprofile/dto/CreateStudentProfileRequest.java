package ucu.retojulio2026.talent.studentprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ucu.retojulio2026.talent.common.DocumentBearer;
import ucu.retojulio2026.talent.common.DocumentType;
import ucu.retojulio2026.talent.common.validation.ValidDocumentNumber;

import java.util.List;

@ValidDocumentNumber
public record CreateStudentProfileRequest(

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
        List<String> skills,

        @Schema(description = "Descripción del perfil del alumno",  example = "Estudiante de Licienciatura en Informática interesado en encontrar mi primera experiencia laboral")
        String description

) implements DocumentBearer {}
