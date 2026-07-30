package ucu.retojulio2026.talent.company;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.vacancy.Vacancy;
import ucu.retojulio2026.talent.vacancy.VacancyService;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplication;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyDeletionServiceImplTest {

    @Mock
    private CompanyService companyService;

    @Mock
    private VacancyService vacancyService;

    @Mock
    private VacancyApplicationService vacancyApplicationService;

    private Vacancy vacancy(String vacancyId) {
        Vacancy vacancy = new Vacancy();
        vacancy.setVacancyId(vacancyId);
        return vacancy;
    }

    private VacancyApplication application(String applicationId) {
        VacancyApplication application = new VacancyApplication();
        application.setVacancyApplicationId(applicationId);
        return application;
    }

    @Test
    void borrar_una_company_inexistente_lanza_resource_not_found_exception() {
        CompanyDeletionServiceImpl service = new CompanyDeletionServiceImpl(
                companyService, vacancyService, vacancyApplicationService);
        when(companyService.existsById("company-x")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.delete("company-x"));

        verify(vacancyService, never()).getByCompanyId(anyString());
        verify(companyService, never()).delete(anyString());
    }

    @Test
    void baja_de_empresa_en_cascada_borra_postulaciones_puestos_y_recien_la_company_en_ese_orden() {
        CompanyDeletionServiceImpl service = new CompanyDeletionServiceImpl(
                companyService, vacancyService, vacancyApplicationService);
        Vacancy vacancy = vacancy("vacancy-1");
        VacancyApplication application = application("application-1");
        when(companyService.existsById("company-1")).thenReturn(true);
        when(vacancyService.getByCompanyId("company-1")).thenReturn(List.of(vacancy));
        when(vacancyApplicationService.getByVacancyId("vacancy-1")).thenReturn(List.of(application));

        service.delete("company-1");

        InOrder inOrder = inOrder(vacancyApplicationService, vacancyService, companyService);
        inOrder.verify(vacancyApplicationService).delete("application-1");
        inOrder.verify(vacancyService).deleteVacancy("vacancy-1");
        inOrder.verify(companyService).delete("company-1");
    }

    @Test
    void baja_de_empresa_sin_puestos_no_borra_puestos_ni_postulaciones_pero_borra_la_company() {
        CompanyDeletionServiceImpl service = new CompanyDeletionServiceImpl(
                companyService, vacancyService, vacancyApplicationService);
        when(companyService.existsById("company-1")).thenReturn(true);
        when(vacancyService.getByCompanyId("company-1")).thenReturn(List.of());

        service.delete("company-1");

        verify(vacancyApplicationService, never()).getByVacancyId(anyString());
        verify(vacancyService, never()).deleteVacancy(anyString());
        verify(companyService).delete("company-1");
    }

    @Test
    void baja_de_empresa_con_puesto_sin_postulaciones_borra_el_puesto_sin_intentar_borrar_postulaciones() {
        CompanyDeletionServiceImpl service = new CompanyDeletionServiceImpl(
                companyService, vacancyService, vacancyApplicationService);
        Vacancy vacancy = vacancy("vacancy-1");
        when(companyService.existsById("company-1")).thenReturn(true);
        when(vacancyService.getByCompanyId("company-1")).thenReturn(List.of(vacancy));
        when(vacancyApplicationService.getByVacancyId("vacancy-1")).thenReturn(List.of());

        service.delete("company-1");

        verify(vacancyApplicationService, never()).delete(anyString());
        verify(vacancyService).deleteVacancy("vacancy-1");
        verify(companyService).delete("company-1");
    }
}
