package ucu.retojulio2026.talent.user.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import ucu.retojulio2026.talent.user.AccountStatus;
import ucu.retojulio2026.talent.user.Role;

public record UserResponse(
        @Schema(description = "Id del usuario (NanoID)", example = "V1StGXR8_Z5j")
        String userId,

        @Schema(description = "Email del usuario", example = "nicogon@ucu.edu.uy")
        String email,

        @Schema(description = "Rol del usuario", example = "ALUMNO")
        Role role,

        @Schema(description = "Estado de admision de la cuenta", example = "PENDIENTE")
        AccountStatus status,

        @Schema(description = "Fecha de alta del usuario", example = "2026-07-15")
        LocalDate registeredAt,

        @Schema(description = "Imagen de perfil del usuario", example = "users/profile-images/V6SgGZR2Z5j.png")
        String profileImage
) {}
