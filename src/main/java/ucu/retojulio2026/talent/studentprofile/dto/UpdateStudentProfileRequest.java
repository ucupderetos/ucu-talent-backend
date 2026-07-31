package ucu.retojulio2026.talent.studentprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record UpdateStudentProfileRequest(

        @Schema(description = "Numero de telefono. Omitir o null para no modificarlo, \"\" para borrarlo",
                example = "+59899123456")
        String phoneNumber,

        @Schema(description = "URL de LinkedIn. Omitir o null para no modificarlo, \"\" para borrarlo",
                example = "https://linkedin.com/in/nicolas-gonzalez")
        String linkedinUrl,

        @Schema(description = "Skills del alumno. Omitir o null para no modificarlas, [] para borrarlas",
                example = "[\"Java\", \"Spring Boot\", \"SQL\"]")
        List<String> skills,

        @Schema(description = "Descripción del perfil del alumno. Omitir o null para no modificarla, \"\" para borrarla",
                example = "Estudiante de Licienciatura en Informática interesado en encontrar mi primera experiencia laboral")
        String description

) {}
