package ucu.retojulio2026.talent.universityregistry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ucu.retojulio2026.talent.common.DocumentType;

public record UpdateUniversityRegistryRequest(

        @Schema(description = "Tipo de documento", example = "CEDULA_IDENTIDAD")
        @NotNull(message = "El tipo de documento es obligatorio")
        DocumentType documentType,

        @Schema(description = "Numero de documento", example = "1.234.567-8")
        @NotBlank(message = "El numero de documento es obligatorio")
        String documentNumber,

        @Schema(description = "Nombre", example = "Nicolas")
        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @Schema(description = "Apellido", example = "Gonzalez")
        @NotBlank(message = "El apellido es obligatorio")
        String surname

) {}
