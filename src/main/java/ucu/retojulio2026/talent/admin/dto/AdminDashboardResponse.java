package ucu.retojulio2026.talent.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ucu.retojulio2026.talent.vacancy.VacancyStatus;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationStatus;

import java.time.LocalDate;
import java.util.List;

public record AdminDashboardResponse(

        @Schema(description = "Totales para las tarjetas del dashboard")
        Counts counts,

        @Schema(description = "Cantidad de postulaciones por estado")
        List<ApplicationStatusCount> applicationStatusSummary,

        @Schema(description = "Ultimos 5 puestos publicados, mas recientes primero")
        List<RecentVacancy> recentVacancies,

        @Schema(description = "Hasta 10 empresas en PENDIENTE, las que se registraron ultimo primero")
        List<PendingCompany> pendingCompanies
) {

    public record Counts(
            CompanyCounts companies,
            VacancyCounts vacancies,
            ApplicationCounts applications,
            UserCounts users
    ) {}

    public record CompanyCounts(
            @Schema(description = "Empresas con perfil creado", example = "42")
            long total,

            @Schema(description = "Empresas cuya cuenta esta en PENDIENTE", example = "7")
            long pendientes
    ) {}

    public record VacancyCounts(
            @Schema(description = "Puestos no eliminados", example = "120")
            long total,

            @Schema(description = "Puestos en PUBLICADO", example = "85")
            long publicadas
    ) {}

    public record ApplicationCounts(
            @Schema(description = "Postulaciones totales", example = "530")
            long total,

            @Schema(description = "Postulaciones en PENDIENTE", example = "64")
            long pendientes
    ) {}

    public record UserCounts(
            @Schema(description = "Cuentas totales", example = "310")
            long total,

            @Schema(description = "Cuentas con rol ALUMNO", example = "260")
            long alumnos,

            @Schema(description = "Cuentas con rol EMPRESA", example = "46")
            long empresas,

            @Schema(description = "Cuentas con rol ADMIN", example = "4")
            long admins
    ) {}

    public record ApplicationStatusCount(
            @Schema(description = "Estado de la postulacion", example = "PENDIENTE")
            VacancyApplicationStatus status,

            @Schema(description = "Cantidad de postulaciones en ese estado", example = "64")
            long count
    ) {}

    public record RecentVacancy(
            @Schema(description = "Id del puesto", example = "V1StGXR8_Z5j")
            String vacancyId,

            @Schema(description = "Nombre del puesto", example = "Desarrollador Java Semi Senior")
            String name,

            @Schema(description = "Nombre de la empresa dueña", example = "Acme S.A.")
            String companyName,

            @Schema(description = "Fecha de publicacion", example = "2026-07-28")
            LocalDate publicationDate,

            @Schema(description = "Estado del puesto", example = "PUBLICADO")
            VacancyStatus status,

            @Schema(description = "Cantidad de postulaciones al puesto", example = "12")
            long applicationCount
    ) {}

    public record PendingCompany(
            @Schema(description = "Id de la empresa", example = "V1StGXR8_Z5j")
            String companyId,

            @Schema(description = "Razon social", example = "Acme S.A.")
            String name,

            @Schema(description = "Industria o rubro", example = "Tecnologia")
            String industry,

            @Schema(description = "Fecha de alta de la cuenta", example = "2026-07-20")
            LocalDate registeredAt
    ) {}
}
