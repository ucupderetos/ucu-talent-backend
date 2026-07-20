package ucu.retojulio2026.talent.dev;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDevAdminRequest(

        @Schema(description = "Email unico del admin", example = "admin@ucu.edu.uy")
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        String email,

        @Schema(description = "Contraseña en texto plano (minimo 8 caracteres)", example = "unaClaveSegura123")
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password,

        @Schema(description = "Nombre del admin", example = "Juan")
        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @Schema(description = "Apellido del admin", example = "Lopez")
        @NotBlank(message = "El apellido es obligatorio")
        String surname

) {}
