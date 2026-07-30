package ucu.retojulio2026.talent.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ucu.retojulio2026.talent.admin.AdminService;
import ucu.retojulio2026.talent.company.CompanyDeletionService;
import ucu.retojulio2026.talent.company.CompanyService;
import ucu.retojulio2026.talent.education.Education;
import ucu.retojulio2026.talent.education.EducationService;
import ucu.retojulio2026.talent.studentprofile.StudentProfileService;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplication;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationService;
import ucu.retojulio2026.talent.workexperience.WorkExperience;
import ucu.retojulio2026.talent.workexperience.WorkExperienceService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountFacadeImplTest {

    @Mock
    private UserService userService;

    @Mock
    private StudentProfileService studentProfileService;

    @Mock
    private CompanyService companyService;

    @Mock
    private CompanyDeletionService companyDeletionService;

    @Mock
    private AdminService adminService;

    @Mock
    private EducationService educationService;

    @Mock
    private WorkExperienceService workExperienceService;

    @Mock
    private VacancyApplicationService vacancyApplicationService;

    private AccountFacadeImpl newFacade() {
        return new AccountFacadeImpl(userService, studentProfileService, companyService, companyDeletionService,
                adminService, educationService, workExperienceService, vacancyApplicationService);
    }

    private User newUser(String userId, Role role) {
        User user = new User();
        user.setUserId(userId);
        user.setRole(role);
        return user;
    }

    @Test
    void revisar_cuenta_de_alumno_llama_a_student_profile_service_review_y_actualiza_el_status() {
        AccountFacadeImpl facade = newFacade();
        when(userService.getById("user-1")).thenReturn(newUser("user-1", Role.ALUMNO));

        facade.reviewAccount("user-1", AccountStatus.APROBADO, "ok");

        verify(userService).updateStatus("user-1", AccountStatus.APROBADO);
        verify(studentProfileService).review(eq("user-1"), any(), eq("ok"));
        verify(companyService, never()).review(anyString(), any(), anyString());
    }

    @Test
    void revisar_cuenta_de_empresa_llama_a_company_service_review_y_actualiza_el_status() {
        AccountFacadeImpl facade = newFacade();
        when(userService.getById("user-2")).thenReturn(newUser("user-2", Role.EMPRESA));

        facade.reviewAccount("user-2", AccountStatus.RECHAZADO, "falta info");

        verify(userService).updateStatus("user-2", AccountStatus.RECHAZADO);
        verify(companyService).review(eq("user-2"), any(), eq("falta info"));
        verify(studentProfileService, never()).review(anyString(), any(), anyString());
    }

    @Test
    void revisar_cuenta_de_admin_no_llama_a_ningun_service_de_perfil_pero_actualiza_el_status() {
        AccountFacadeImpl facade = newFacade();
        when(userService.getById("user-3")).thenReturn(newUser("user-3", Role.ADMIN));

        facade.reviewAccount("user-3", AccountStatus.APROBADO, null);

        verify(userService).updateStatus("user-3", AccountStatus.APROBADO);
        verify(studentProfileService, never()).review(anyString(), any(), anyString());
        verify(companyService, never()).review(anyString(), any(), anyString());
    }

    @Test
    void saber_si_tiene_perfil_delega_en_student_profile_service_para_alumno() {
        AccountFacadeImpl facade = newFacade();
        when(userService.getById("user-1")).thenReturn(newUser("user-1", Role.ALUMNO));
        when(studentProfileService.existsById("user-1")).thenReturn(true);

        boolean result = facade.hasProfile("user-1");

        assertThat(result).isTrue();
        verify(companyService, never()).existsById(anyString());
        verify(adminService, never()).existsById(anyString());
    }

    @Test
    void saber_si_tiene_perfil_delega_en_company_service_para_empresa() {
        AccountFacadeImpl facade = newFacade();
        when(userService.getById("user-2")).thenReturn(newUser("user-2", Role.EMPRESA));
        when(companyService.existsById("user-2")).thenReturn(false);

        boolean result = facade.hasProfile("user-2");

        assertThat(result).isFalse();
        verify(studentProfileService, never()).existsById(anyString());
        verify(adminService, never()).existsById(anyString());
    }

    @Test
    void saber_si_tiene_perfil_delega_en_admin_service_para_admin() {
        AccountFacadeImpl facade = newFacade();
        when(userService.getById("user-3")).thenReturn(newUser("user-3", Role.ADMIN));
        when(adminService.existsById("user-3")).thenReturn(true);

        boolean result = facade.hasProfile("user-3");

        assertThat(result).isTrue();
        verify(studentProfileService, never()).existsById(anyString());
        verify(companyService, never()).existsById(anyString());
    }

    @Test
    void borrar_cuenta_de_alumno_sin_perfil_no_ejecuta_cascada_pero_borra_el_usuario() {
        AccountFacadeImpl facade = newFacade();
        when(userService.getById("user-1")).thenReturn(newUser("user-1", Role.ALUMNO));
        when(studentProfileService.existsById("user-1")).thenReturn(false);

        facade.deleteAccount("user-1");

        verify(educationService, never()).getByStudentProfileId(anyString());
        verify(studentProfileService, never()).delete(anyString());
        verify(userService).delete("user-1");
    }

    @Test
    void borrar_cuenta_de_alumno_borra_educaciones_experiencias_y_postulaciones_antes_del_perfil() {
        AccountFacadeImpl facade = newFacade();
        Education education = new Education();
        education.setEducationId("education-1");
        WorkExperience workExperience = new WorkExperience();
        workExperience.setWorkExperienceId("work-1");
        VacancyApplication application = new VacancyApplication();
        application.setVacancyApplicationId("application-1");
        when(userService.getById("user-1")).thenReturn(newUser("user-1", Role.ALUMNO));
        when(studentProfileService.existsById("user-1")).thenReturn(true);
        when(educationService.getByStudentProfileId("user-1")).thenReturn(List.of(education));
        when(workExperienceService.getByStudentProfileId("user-1")).thenReturn(List.of(workExperience));
        when(vacancyApplicationService.getByStudentProfileId("user-1")).thenReturn(List.of(application));

        facade.deleteAccount("user-1");

        InOrder inOrder = inOrder(educationService, workExperienceService, vacancyApplicationService,
                studentProfileService, userService);
        inOrder.verify(educationService).delete("education-1");
        inOrder.verify(workExperienceService).delete("work-1");
        inOrder.verify(vacancyApplicationService).delete("application-1");
        inOrder.verify(studentProfileService).delete("user-1");
        inOrder.verify(userService).delete("user-1");
    }

    @Test
    void borrar_cuenta_de_empresa_con_perfil_delega_en_company_deletion_service() {
        AccountFacadeImpl facade = newFacade();
        when(userService.getById("user-2")).thenReturn(newUser("user-2", Role.EMPRESA));
        when(companyService.existsById("user-2")).thenReturn(true);

        facade.deleteAccount("user-2");

        verify(companyDeletionService).delete("user-2");
        verify(userService).delete("user-2");
    }

    @Test
    void borrar_cuenta_de_empresa_sin_perfil_no_delega_en_company_deletion_service() {
        AccountFacadeImpl facade = newFacade();
        when(userService.getById("user-2")).thenReturn(newUser("user-2", Role.EMPRESA));
        when(companyService.existsById("user-2")).thenReturn(false);

        facade.deleteAccount("user-2");

        verify(companyDeletionService, never()).delete(anyString());
        verify(userService).delete("user-2");
    }

    @Test
    void borrar_cuenta_de_admin_no_delega_en_ningun_service_de_perfil_solo_borra_el_usuario() {
        AccountFacadeImpl facade = newFacade();
        when(userService.getById("user-3")).thenReturn(newUser("user-3", Role.ADMIN));

        facade.deleteAccount("user-3");

        verify(studentProfileService, never()).existsById(anyString());
        verify(companyService, never()).existsById(anyString());
        verify(companyDeletionService, never()).delete(anyString());
        verify(userService).delete("user-3");
    }
}
