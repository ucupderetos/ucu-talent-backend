package ucu.retojulio2026.talent.mail.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import ucu.retojulio2026.talent.mail.MailTemplateCode;

public record MailTemplateResponse(

        @Schema(description = "Id del template (NanoID)", example = "AbC123xYz890")
        String mailTemplateId,

        @Schema(description = "Codigo del template", example = "APPLICATION_VISTO")
        MailTemplateCode code,

        @Schema(description = "Asunto del mail", example = "Tu postulación fue vista")
        String subject,

        @Schema(description = "Cuerpo del mail, admite placeholders {{variable}}")
        String body,

        @Schema(description = "Placeholders disponibles para este template", example = "[\"studentName\", \"vacancyName\"]")
        List<String> placeholders) {
}
