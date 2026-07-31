package ucu.retojulio2026.talent.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateAdminRequest(

        @Schema(description = "Nombre del admin", example = "Juan")
        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @Schema(description = "Apellido del admin", example = "Lopez")
        @NotBlank(message = "El apellido es obligatorio")
        String surname

) {}
