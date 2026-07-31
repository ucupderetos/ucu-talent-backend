package ucu.retojulio2026.talent.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminResponse(
        @Schema(description = "Id del admin (NanoID). Es el mismo valor que el userId.", example = "V1StGXR8_Z5j")
        String adminId,

        @Schema(description = "Nombre del admin", example = "Juan")
        String name,

        @Schema(description = "Apellido del admin", example = "Lopez")
        String surname
) {}
