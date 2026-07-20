package ucu.retojulio2026.talent.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

//Datos que la API RECIBE para crear un admin.
//No lleva adminId: la PK es el userId (PK compartida), la asigna el mapper.
public record CreateAdminRequest(

        @Schema(description = "Id del usuario dueño del admin (NanoID de 12 caracteres)", example = "V1StGXR8_Z5j")
        @NotBlank(message = "El userId es obligatorio")
        String userId,

        @Schema(description = "Nombre del admin", example = "Juan")
        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @Schema(description = "Apellido del admin", example = "Lopez")
        @NotBlank(message = "El apellido es obligatorio")
        String surname

) {}
