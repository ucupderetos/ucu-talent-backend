package ucu.retojulio2026.talent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ucu.retojulio2026.talent.education.Education;
import ucu.retojulio2026.talent.education.EducationService;
import ucu.retojulio2026.talent.mail.MailService;
import ucu.retojulio2026.talent.studentprofile.StudentProfile;
import ucu.retojulio2026.talent.studentprofile.StudentProfileService;
import ucu.retojulio2026.talent.user.AccountStatus;
import ucu.retojulio2026.talent.user.User;
import ucu.retojulio2026.talent.user.UserService;
import ucu.retojulio2026.talent.vacancy.Vacancy;
import ucu.retojulio2026.talent.vacancy.VacancyServiceImpl;
import ucu.retojulio2026.talent.vacancy.VacancyStatus;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplication;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationRepository;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationServiceImpl;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationStatus;
import ucu.retojulio2026.talent.vacancyapplication.dto.CreateVacancyApplicationRequest;
import ucu.retojulio2026.talent.vacancyapplication.dto.VacancyApplicationMapper;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VacancyApplicationServiceImplTest {

    @Mock private VacancyApplicationRepository vacancyApplicationRepository;
    @Mock private StudentProfileService studentProfileService;
    @Mock private EducationService educationService;
    @Mock private VacancyApplicationMapper vacancyApplicationMapper;
    @Mock private VacancyServiceImpl vacancyService;
    @Mock private UserService userService;
    @Mock private MailService mailService;

    @InjectMocks private VacancyApplicationServiceImpl service;

    @Test
    void create_caminoFeliz_guardaConStatusPendienteYAppliedAt() {
        String vacancyId = "vac123456789";
        String studentId = "stu123456789";
        String companyId = "cmp123456789";

        CreateVacancyApplicationRequest request =
                new CreateVacancyApplicationRequest(vacancyId, studentId, null, null);

        Vacancy vacancy = new Vacancy();
        vacancy.setVacancyId(vacancyId);
        vacancy.setCompanyId(companyId);
        vacancy.setName("Backend Developer");
        vacancy.setStatus(VacancyStatus.PUBLICADO);

        User studentUser = new User();
        studentUser.setStatus(AccountStatus.APROBADO);

        User companyUser = new User();
        companyUser.setEmail("contacto@qsy.com");

        StudentProfile applicant = new StudentProfile();
        applicant.setName("Nico");
        applicant.setSurname("Perez");

        VacancyApplication entity = new VacancyApplication();
        entity.setVacancyId(vacancyId);
        entity.setStudentProfileId(studentId);

        when(vacancyService.getVacancyById(vacancyId)).thenReturn(vacancy);
        when(studentProfileService.existsById(studentId)).thenReturn(true);
        when(userService.getById(studentId)).thenReturn(studentUser);
        when(educationService.getByStudentProfileId(studentId)).thenReturn(List.of(new Education()));
        when(vacancyApplicationRepository.existsByVacancyIdAndStudentProfileId(vacancyId, studentId)).thenReturn(false);
        when(vacancyApplicationMapper.toEntity(request)).thenReturn(entity);
        when(vacancyApplicationRepository.save(any(VacancyApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(studentProfileService.getById(studentId)).thenReturn(applicant);
        when(userService.getById(companyId)).thenReturn(companyUser);

        VacancyApplication result = service.create(request);

        assertEquals(VacancyApplicationStatus.PENDIENTE, result.getStatus());
        assertNotNull(result.getAppliedAt());
        assertEquals(LocalDate.now(), result.getAppliedAt());

        verify(vacancyApplicationRepository).save(entity);
        verify(mailService).sendCompanyNewApplicationEmail(
                "contacto@qsy.com", "Nico Perez", "Backend Developer");
    }
}