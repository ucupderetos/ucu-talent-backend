package ucu.retojulio2026.talent.studentprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdateStudentProfileRequest(

        @Schema(description = "Numero de telefono", example = "+59899123456")
        @NotBlank(message = "El phoneNumber es obligatorio")
        String phoneNumber,

        @Schema(description = "URL de LinkedIn", example = "https://linkedin.com/in/nicolas-gonzalez")
        @NotBlank(message = "El linkedinUrl es obligatorio")
        String linkedinUrl,

        @Schema(description = "Skills del alumno", example = "[\"Java\", \"Spring Boot\", \"SQL\"]")
        @NotEmpty(message = "El skills es obligatorio")
        List<String> skills

) {}
