package ucu.retojulio2026.talent.studentprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

//Datos que la API RECIBE para crear un perfil de alumno.
//Usar records garantiza inmutabilidad y evita boilerplate (getters, setters, equals, etc)
public record CreateStudentProfileRequest(

        @Schema(description = "Id del usuario dueño del perfil (NanoID de 12 caracteres)", example = "V1StGXR8_Z5j")
        @NotBlank(message = "El userId es obligatorio")
        String userId,

        @Schema(description = "Skills del alumno, cargadas desde el frontend", example = "[\"Java\", \"Spring Boot\", \"SQL\"]")
        List<String> skills

) {}
