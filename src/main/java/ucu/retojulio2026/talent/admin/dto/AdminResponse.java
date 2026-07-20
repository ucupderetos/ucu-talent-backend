package ucu.retojulio2026.talent.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

//Datos que la API DEVUELVE de un admin.
//No lleva userId: la PK ya es el userId (PK compartida, ver Admin).
public record AdminResponse(
        @Schema(description = "Id del admin (NanoID). Es el mismo valor que el userId.", example = "V1StGXR8_Z5j")
        String adminId,

        @Schema(description = "Nombre del admin", example = "Juan")
        String name,

        @Schema(description = "Apellido del admin", example = "Lopez")
        String surname
) {}
