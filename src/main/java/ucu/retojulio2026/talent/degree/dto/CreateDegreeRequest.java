package ucu.retojulio2026.talent.degree.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

//Datos que la API RECIBE para crear una carrera.
//Usar records garantiza inmutabilidad y evita boilerplate (getters, setters, equals, etc)
public record CreateDegreeRequest(

        @Schema(description = "Id del area a la que pertenece la carrera", example = "V1StGXR8_Z5j")
        @NotNull
        @NotBlank(message = "El areaId es obligatorio")
        String areaId,

        @Schema(description = "Nombre de la carrera", example = "Licenciatura en Informatica")
        @NotNull
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String name,

        @Schema(description = "Indica si la carrera pertenece a la UCU", example = "true")
        @NotNull(message = "isUcu es obligatorio")
        Boolean isUcu) {
}