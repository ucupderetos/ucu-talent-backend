package ucu.retojulio2026.talent.area.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AreaResponse(

        @Schema(description = "Id del area (NanoID)", example = "V1StGXR8_Z5j")
        String areaId,

        @Schema(description = "Nombre del area", example = "Tecnologia")
        String name,

        @Schema(description = "Id del area padre (null si es raíz)", example = "aB3dEfGhIjKl")
        String parentAreaId

) {}