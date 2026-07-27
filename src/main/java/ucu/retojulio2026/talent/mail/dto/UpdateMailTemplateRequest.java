package ucu.retojulio2026.talent.mail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMailTemplateRequest(

        @Schema(description = "Asunto del mail", example = "Tu postulación fue vista")
        @NotBlank(message = "El subject es obligatorio")
        @Size(max = 200, message = "El subject no puede superar los 200 caracteres")
        String subject,

        @Schema(description = "Cuerpo del mail, admite placeholders {{variable}}")
        @NotBlank(message = "El body es obligatorio")
        String body) {
}
