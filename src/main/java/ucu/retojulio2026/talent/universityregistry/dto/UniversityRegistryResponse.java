package ucu.retojulio2026.talent.universityregistry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ucu.retojulio2026.talent.universityregistry.DocumentType;

public record UniversityRegistryResponse(

        @Schema(description = "Id del registro (NanoID)", example = "V1StGXR8_Z5j")
        String universityRegistryId,

        @Schema(description = "Tipo de documento", example = "CEDULA")
        DocumentType documentType,

        @Schema(description = "Numero de documento", example = "1.234.567-8")
        String documentNumber,

        @Schema(description = "Nombre", example = "Nicolas")
        String name,

        @Schema(description = "Apellido", example = "Gonzalez")
        String surname

) {}
