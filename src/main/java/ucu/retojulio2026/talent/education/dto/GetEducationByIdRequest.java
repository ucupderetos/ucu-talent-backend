package ucu.retojulio2026.talent.education.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record GetEducationByIdRequest(

        @Schema(description = "Id de education (NanoID de 12 caracteres)", example = "V8k3nPq1Xz7B")
        @NotBlank(message = "El id es obligatorio")
        String educationId

) {}
