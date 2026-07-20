package ucu.retojulio2026.talent.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import ucu.retojulio2026.talent.common.Department;

public record CompanyResponse(
        @Schema(description = "Id de la empresa (NanoID)", example = "V1StGXR8_Z5j")
        String companyId,

        @Schema(description = "Razon social de la empresa", example = "ACME S.A.")
        String name,

        @Schema(description = "Industria o rubro de la empresa", example = "Tecnologia")
        String industry,

        @Schema(description = "Descripcion de la empresa", example = "Consultora de software a medida")
        String description,

        @Schema(description = "URL del sitio web de la empresa", example = "https://company.com")
        String webUrl,

        @Schema(description = "URL del LinkedIn de la empresa", example = "https://linkedin.com/company/acme")
        String linkedinUrl,

        @Schema(description = "Departamento donde esta ubicada la empresa", example = "MONTEVIDEO")
        Department location
) {}
