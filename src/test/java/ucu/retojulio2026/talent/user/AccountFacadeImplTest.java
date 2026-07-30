package ucu.retojulio2026.talent.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ucu.retojulio2026.talent.admin.AdminService;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

    private AccountFacadeImpl accountFacade;

    @BeforeEach
    void setUp() {
        accountFacade = new AccountFacadeImpl(
                userService,
                studentProfileService,
                companyService,
                companyDeletionService,
                adminService,
                educationService,
                workExperienceService,
                vacancyApplicationService
        );
    }

    @Test
    void eliminar_alumno_borra_datos_en_cascada_y_al_final_el_usuario() {
        String userId = "student-1";

        User user = new User();
        user.setRole(Role.ALUMNO);

        Education education = new Education();
        education.setEducationId("education-1");

        WorkExperience workExperience = new WorkExperience();
        workExperience.setWorkExperienceId("experience-1");

        VacancyApplication application = new VacancyApplication();
        application.setVacancyApplicationId("application-1");

        when(userService.getById(userId))
                .thenReturn(user);

        when(studentProfileService.existsById(userId))
                .thenReturn(true);

        when(educationService.getByStudentProfileId(userId))
                .thenReturn(List.of(education));

        when(workExperienceService.getByStudentProfileId(userId))
                .thenReturn(List.of(workExperience));

        when(vacancyApplicationService.getByStudentProfileId(userId))
                .thenReturn(List.of(application));

        accountFacade.deleteAccount(userId);

        InOrder inOrder = inOrder(
                educationService,
                workExperienceService,
                vacancyApplicationService,
                studentProfileService,
                userService
        );

        inOrder.verify(educationService)
                .delete("education-1");

        inOrder.verify(workExperienceService)
                .delete("experience-1");

        inOrder.verify(vacancyApplicationService)
                .delete("application-1");

        inOrder.verify(studentProfileService)
                .delete(userId);

        inOrder.verify(userService)
                .delete(userId);
    }

    @Test
    void eliminar_admin_borra_solo_el_usuario_sin_cascada() {
        String userId = "admin-1";

        User user = new User();
        user.setRole(Role.ADMIN);

        when(userService.getById(userId))
                .thenReturn(user);

        accountFacade.deleteAccount(userId);

        verify(userService).delete(userId);

        verifyNoInteractions(
                educationService,
                workExperienceService,
                vacancyApplicationService,
                companyService,
                companyDeletionService,
                adminService
        );

        verify(studentProfileService, never())
                .existsById(userId);

        verify(studentProfileService, never())
                .delete(userId);
    }

    @Test
    void eliminar_alumno_sin_perfil_no_explota_y_borra_el_usuario() {
        String userId = "student-1";

        User user = new User();
        user.setRole(Role.ALUMNO);

        when(userService.getById(userId))
                .thenReturn(user);

        when(studentProfileService.existsById(userId))
                .thenReturn(false);

        assertDoesNotThrow(
                () -> accountFacade.deleteAccount(userId)
        );

        verify(studentProfileService).existsById(userId);
        verify(userService).delete(userId);

        verifyNoInteractions(
                educationService,
                workExperienceService,
                vacancyApplicationService
        );

        verify(studentProfileService, never())
                .delete(userId);
    }

    @Test
    void eliminar_usuario_inexistente_lanza_resource_not_found_exception() {
        String userId = "user-inexistente";

        when(userService.getById(userId))
                .thenThrow(ResourceNotFoundException.class);

        assertThrows(
                ResourceNotFoundException.class,
                () -> accountFacade.deleteAccount(userId)
        );

        verify(userService, never()).delete(userId);

        verifyNoInteractions(
                studentProfileService,
                educationService,
                workExperienceService,
                vacancyApplicationService,
                companyService,
                companyDeletionService,
                adminService
        );
    }
}
