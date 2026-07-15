package ucu.retojulio2026.talent.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @Schema(description = "Email del usuario", example = "washi@ucu.edu.uy")
        @NotBlank(message = "El email es obligatorio")
        String email,

        @Schema(description = "Contraseña en texto plano", example = "unaClaveSegura123")
        @NotBlank(message = "La contraseña es obligatoria")
        String password

) {}
