package ucu.retojulio2026.talent.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ucu.retojulio2026.talent.admin.AdminService;
import ucu.retojulio2026.talent.company.CompanyDeletionService;
import ucu.retojulio2026.talent.company.CompanyService;
import ucu.retojulio2026.talent.education.EducationService;
import ucu.retojulio2026.talent.studentprofile.StudentProfileService;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationService;
import ucu.retojulio2026.talent.workexperience.WorkExperienceService;

import static org.junit.jupiter.api.Assertions.assertTrue;
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
    void tiene_perfil_consulta_student_profile_si_el_usuario_es_alumno() {
        String userId = "user-1";

        User user = new User();
        user.setRole(Role.ALUMNO);

        when(userService.getById(userId))
                .thenReturn(user);

        when(studentProfileService.existsById(userId))
                .thenReturn(true);

        boolean result = accountFacade.hasProfile(userId);

        assertTrue(result);

        verify(userService).getById(userId);
        verify(studentProfileService).existsById(userId);
    }
}