package ucu.retojulio2026.talent.studentprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

//Datos que la API DEVUELVE de un perfil de alumno.
public record StudentProfileResponse(
        @Schema(description = "Id del perfil de alumno (NanoID)", example = "V1StGXR8_Z5j")
        String studentProfileId,

        @Schema(description = "Id del usuario dueño del perfil", example = "aB3dEfGhIjKl")
        String userId,

        @Schema(description = "Skills del alumno", example = "[\"Java\", \"Spring Boot\"]")
        List<String> skills
) {}
