package ucu.retojulio2026.talent.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ucu.retojulio2026.talent.common.Department;

public record CreateCompanyRequest(

        @Schema(description = "Razon social de la empresa", example = "ACME S.A.")
        @NotBlank(message = "La razon social es obligatoria")
        String name,

        @Schema(description = "Industria o rubro de la empresa", example = "Tecnologia")
        @NotBlank(message = "La industria es obligatoria")
        String industry,

        @Schema(description = "Descripcion de la empresa", example = "Consultora de software a medida")
        @NotBlank(message = "La descripcion es obligatoria")
        String description,

        @Schema(description = "URL del sitio web de la empresa", example = "https://company.com")
        @NotBlank(message = "La web es obligatoria")
        String webUrl,

        @Schema(description = "URL del LinkedIn de la empresa", example = "https://linkedin.com/company/acme")
        @NotBlank(message = "El LinkedIn es obligatorio")
        String linkedinUrl,

        @Schema(description = "Departamento donde esta ubicada la empresa", example = "MONTEVIDEO")
        @NotNull(message = "La ubicacion es obligatoria")
        Department location

) {}
