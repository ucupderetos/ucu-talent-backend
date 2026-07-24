package ucu.retojulio2026.talent.auth.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import ucu.retojulio2026.talent.user.AccountStatus;
import ucu.retojulio2026.talent.user.Role;

public record MeResponse(
        @Schema(description = "Id del usuario (NanoID)", example = "V1StGXR8_Z5j")
        String userId,

        @Schema(description = "Email del usuario", example = "nicogon@ucu.edu.uy")
        String email,

        @Schema(description = "Rol del usuario", example = "ALUMNO")
        Role role,

        @Schema(description = "Estado de admision de la cuenta. Aplica a los tres roles.", example = "PENDIENTE")
        AccountStatus status,

        @Schema(description = "Fecha de alta del usuario", example = "2026-07-15")
        LocalDate registeredAt,

        @Schema(description = "Si ya existe el perfil (StudentProfile/Company/Admin) del paso 2 del registro.", example = "true")
        Boolean hasProfile
) {}
