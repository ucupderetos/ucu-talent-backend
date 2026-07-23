package ucu.retojulio2026.talent.vacancy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ucu.retojulio2026.talent.common.Department;
import ucu.retojulio2026.talent.vacancy.Modality;
import ucu.retojulio2026.talent.vacancy.VacancyStatus;

public record SearchCriteriaVacancyRequest(

        @Schema(description = "Id del area (incluye subareas)", example = "V1StGXR8_Z5j")
        String areaId,

        @Schema(description = "Id de la carrera (se resuelve via su area)", example = "V1StGXR8_Z5j")
        String degreeId,

        @Schema(description = "Tipo de contrato", example = "Full time")
        String contractType,

        @Schema(description = "Modalidad de trabajo", example = "REMOTO")
        Modality modality,

        @Schema(description = "Localidad (departamento)", example = "MONTEVIDEO")
        Department location,

        @Schema(description = "Palabra clave, busca en nombre y descripcion", example = "Java")
        String keyword,

        @Schema(description = "Estado del puesto", example = "PUBLICADO")
        VacancyStatus status
) {
}