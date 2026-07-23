package ucu.retojulio2026.talent.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ucu.retojulio2026.talent.user.AccountStatus;

public record UpdateUserStatusRequest(

        @Schema(description = "Nuevo estado de la cuenta. Solo APROBADO o RECHAZADO.",
                example = "APROBADO",
                allowableValues = {"APROBADO", "RECHAZADO"})
        @NotNull(message = "El estado es obligatorio")
        AccountStatus status,

        @Schema(description = "Comentario del ADMIN", example = "Aprobado, o descripción del motivo de rechazo de aprobación")
        String adminComment

) {}
