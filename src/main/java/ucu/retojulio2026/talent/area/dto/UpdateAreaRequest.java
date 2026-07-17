package ucu.retojulio2026.talent.area.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateAreaRequest(

        @Schema(description = "Nombre del area", example = "Tecnologia")
        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @Schema(description = "Id del area padre (NanoID). Dejar vacío si es un area raíz", example = "V1StGXR8_Z5j")
        String parentAreaId

) {}