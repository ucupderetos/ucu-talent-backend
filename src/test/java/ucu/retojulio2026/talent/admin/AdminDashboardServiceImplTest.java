package ucu.retojulio2026.talent.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ucu.retojulio2026.talent.admin.dto.AdminDashboardResponse;
import ucu.retojulio2026.talent.company.CompanyService;
import ucu.retojulio2026.talent.company.dto.PendingCompanyRow;
import ucu.retojulio2026.talent.user.AccountStatus;
import ucu.retojulio2026.talent.user.Role;
import ucu.retojulio2026.talent.user.UserService;
import ucu.retojulio2026.talent.vacancy.VacancyService;
import ucu.retojulio2026.talent.vacancy.VacancyStatus;
import ucu.retojulio2026.talent.vacancy.dto.RecentVacancyRow;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationService;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationStatus;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceImplTest {

    @Mock private CompanyService companyService;
    @Mock private VacancyService vacancyService;
    @Mock private VacancyApplicationService vacancyApplicationService;
    @Mock private UserService userService;

    @InjectMocks private AdminDashboardServiceImpl service;

    private Map<VacancyApplicationStatus, Long> resumen(long pendiente, long visto, long finalizado) {
        Map<VacancyApplicationStatus, Long> counts = new EnumMap<>(VacancyApplicationStatus.class);
        counts.put(VacancyApplicationStatus.PENDIENTE, pendiente);
        counts.put(VacancyApplicationStatus.VISTO, visto);
        counts.put(VacancyApplicationStatus.FINALIZADO, finalizado);
        return counts;
    }

    private void stubTodo(Map<VacancyApplicationStatus, Long> porEstado) {
        when(vacancyApplicationService.countByStatusSummary()).thenReturn(porEstado);
        when(companyService.count()).thenReturn(2L);
        when(companyService.countByAccountStatus(AccountStatus.PENDIENTE)).thenReturn(1L);
        when(vacancyService.countNotDeleted()).thenReturn(3L);
        when(vacancyService.countPublished()).thenReturn(2L);
        when(vacancyApplicationService.count()).thenReturn(5L);
        when(userService.count()).thenReturn(4L);
        when(userService.countByRole(Role.ALUMNO)).thenReturn(1L);
        when(userService.countByRole(Role.EMPRESA)).thenReturn(2L);
        when(userService.countByRole(Role.ADMIN)).thenReturn(1L);
        when(vacancyService.getRecentForDashboard(5)).thenReturn(List.of());
        when(companyService.getPendingForDashboard(10)).thenReturn(List.of());
    }

    @Test
    void dashboard_arma_los_totales_de_las_cuatro_entidades() {
        stubTodo(resumen(1L, 1L, 3L));

        AdminDashboardResponse.Counts counts = service.getDashboard().counts();

        assertEquals(2L, counts.companies().total());
        assertEquals(1L, counts.companies().pendientes());
        assertEquals(3L, counts.vacancies().total());
        assertEquals(2L, counts.vacancies().publicadas());
        assertEquals(5L, counts.applications().total());
        assertEquals(1L, counts.applications().pendientes());
        assertEquals(4L, counts.users().total());
        assertEquals(1L, counts.users().alumnos());
        assertEquals(2L, counts.users().empresas());
        assertEquals(1L, counts.users().admins());
    }

    @Test
    void dashboard_incluye_todos_los_estados_de_postulacion_aunque_el_conteo_sea_cero() {
        stubTodo(resumen(1L, 0L, 0L));

        List<AdminDashboardResponse.ApplicationStatusCount> resumenEstados =
                service.getDashboard().applicationStatusSummary();

        assertEquals(VacancyApplicationStatus.values().length, resumenEstados.size());
        for (VacancyApplicationStatus status : VacancyApplicationStatus.values()) {
            assertTrue(resumenEstados.stream().anyMatch(x -> x.status() == status));
        }
        assertEquals(0L, resumenEstados.stream()
                .filter(x -> x.status() == VacancyApplicationStatus.FINALIZADO)
                .findFirst().orElseThrow().count());
    }

    @Test
    void dashboard_pide_los_topes_que_definio_el_front() {
        stubTodo(resumen(0L, 0L, 0L));

        service.getDashboard();

        verify(vacancyService).getRecentForDashboard(5);
        verify(companyService).getPendingForDashboard(10);
    }

    @Test
    void dashboard_mapea_los_listados_sin_perder_datos() {
        when(vacancyApplicationService.countByStatusSummary()).thenReturn(resumen(0L, 0L, 0L));
        when(companyService.count()).thenReturn(1L);
        when(companyService.countByAccountStatus(AccountStatus.PENDIENTE)).thenReturn(1L);
        when(vacancyService.countNotDeleted()).thenReturn(1L);
        when(vacancyService.countPublished()).thenReturn(1L);
        when(vacancyApplicationService.count()).thenReturn(0L);
        when(userService.count()).thenReturn(1L);
        when(userService.countByRole(Role.ALUMNO)).thenReturn(0L);
        when(userService.countByRole(Role.EMPRESA)).thenReturn(1L);
        when(userService.countByRole(Role.ADMIN)).thenReturn(0L);
        when(vacancyService.getRecentForDashboard(5)).thenReturn(List.of(
                new RecentVacancyRow("vac111111111", "Java Dev", "ACME S.A.",
                        LocalDate.of(2026, 8, 20), VacancyStatus.PUBLICADO, 7L)));
        when(companyService.getPendingForDashboard(10)).thenReturn(List.of(
                new PendingCompanyRow("cmp111111111", "PENDIENTE S.R.L.", "Retail", LocalDate.of(2026, 7, 20))));

        AdminDashboardResponse result = service.getDashboard();

        AdminDashboardResponse.RecentVacancy vacante = result.recentVacancies().get(0);
        assertEquals("vac111111111", vacante.vacancyId());
        assertEquals("Java Dev", vacante.name());
        assertEquals("ACME S.A.", vacante.companyName());
        assertEquals(LocalDate.of(2026, 8, 20), vacante.publicationDate());
        assertEquals(VacancyStatus.PUBLICADO, vacante.status());
        assertEquals(7L, vacante.applicationCount());

        AdminDashboardResponse.PendingCompany empresa = result.pendingCompanies().get(0);
        assertEquals("cmp111111111", empresa.companyId());
        assertEquals("PENDIENTE S.R.L.", empresa.name());
        assertEquals("Retail", empresa.industry());
        assertEquals(LocalDate.of(2026, 7, 20), empresa.registeredAt());
    }
}
