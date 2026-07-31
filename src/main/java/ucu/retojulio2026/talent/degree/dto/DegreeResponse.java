package ucu.retojulio2026.talent.degree.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record DegreeResponse(

        @Schema(description = "Id de la carrera (NanoID)", example = "AbC123xYz890")
        String degreeId,

        @Schema(description = "Id del area a la que pertenece la carrera", example = "V1StGXR8_Z5j")
        String areaId,

        @Schema(description = "Nombre de la carrera", example = "Licenciatura en Informatica")
        String name,

        @Schema(description = "Indica si la carrera pertenece a la UCU", example = "true")
        Boolean isUcu) {
}
