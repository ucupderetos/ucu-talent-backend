package ucu.retojulio2026.talent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import ucu.retojulio2026.talent.mail.MailService;
import ucu.retojulio2026.talent.studentprofile.StudentProfile;
import ucu.retojulio2026.talent.studentprofile.StudentProfileService;
import ucu.retojulio2026.talent.user.User;
import ucu.retojulio2026.talent.user.UserService;
import ucu.retojulio2026.talent.vacancy.Vacancy;
import ucu.retojulio2026.talent.vacancy.VacancyService;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationCreatedEvent;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationMailNotifier;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationStatus;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationStatusChangedEvent;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VacancyApplicationMailNotifierTest {

    private static final String VACANCY_ID = "vac123456789";
    private static final String STUDENT_ID = "stu123456789";
    private static final String COMPANY_ID = "cmp123456789";

    @Mock private VacancyService vacancyService;
    @Mock private StudentProfileService studentProfileService;
    @Mock private UserService userService;
    @Mock private MailService mailService;

    @InjectMocks private VacancyApplicationMailNotifier notifier;

    private Vacancy vacancy(String name) {
        Vacancy vacancy = new Vacancy();
        vacancy.setVacancyId(VACANCY_ID);
        vacancy.setCompanyId(COMPANY_ID);
        vacancy.setName(name);
        return vacancy;
    }

    private StudentProfile applicant() {
        StudentProfile applicant = new StudentProfile();
        applicant.setName("Nico");
        applicant.setSurname("Perez");
        return applicant;
    }

    private User userWithEmail(String email) {
        User user = new User();
        user.setEmail(email);
        return user;
    }

    private void stubStatusChangedLookups(String vacancyName, String applicantEmail) {
        when(vacancyService.getVacancyById(VACANCY_ID)).thenReturn(vacancy(vacancyName));
        when(studentProfileService.getById(STUDENT_ID)).thenReturn(applicant());
        when(userService.getById(STUDENT_ID)).thenReturn(userWithEmail(applicantEmail));
    }

    @Test
    void onApplicationCreated_avisaALaEmpresaConElNombreCompletoDelPostulante() {
        when(vacancyService.getVacancyById(VACANCY_ID)).thenReturn(vacancy("Backend Developer"));
        when(studentProfileService.getById(STUDENT_ID)).thenReturn(applicant());
        when(userService.getById(COMPANY_ID)).thenReturn(userWithEmail("contacto@qsy.com"));

        notifier.onApplicationCreated(new VacancyApplicationCreatedEvent(VACANCY_ID, STUDENT_ID));

        verify(mailService).sendCompanyNewApplicationEmail(
                "contacto@qsy.com", "Nico Perez", "Backend Developer");
    }

    @Test
    void onApplicationStatusChanged_visto_avisaAlAlumnoQueVieronSuPostulacion() {
        stubStatusChangedLookups("Backend Developer", "nico@test.com");

        notifier.onApplicationStatusChanged(new VacancyApplicationStatusChangedEvent(
                VACANCY_ID, STUDENT_ID, VacancyApplicationStatus.VISTO));

        verify(mailService).sendApplicationVistoEmail(
                "nico@test.com", "Nico Perez", "Backend Developer");
    }

    @Test
    void onApplicationStatusChanged_finalizado_avisaAlAlumnoQueSeCerroLaVacante() {
        stubStatusChangedLookups("Backend Developer", "nico@test.com");

        notifier.onApplicationStatusChanged(new VacancyApplicationStatusChangedEvent(
                VACANCY_ID, STUDENT_ID, VacancyApplicationStatus.FINALIZADO));

        verify(mailService).sendVacancyClosedEmail(
                "nico@test.com", "Nico Perez", "Backend Developer");
    }

    @Test
    void onApplicationStatusChanged_pendiente_noMandaNingunMail() {
        stubStatusChangedLookups("Backend Developer", "nico@test.com");

        notifier.onApplicationStatusChanged(new VacancyApplicationStatusChangedEvent(
                VACANCY_ID, STUDENT_ID, VacancyApplicationStatus.PENDIENTE));

        verifyNoInteractions(mailService);
    }

    @Test
    void ambosListeners_correnReciénDespuesDelCommit() throws NoSuchMethodException {
        Method onCreated = VacancyApplicationMailNotifier.class
                .getMethod("onApplicationCreated", VacancyApplicationCreatedEvent.class);
        Method onStatusChanged = VacancyApplicationMailNotifier.class
                .getMethod("onApplicationStatusChanged", VacancyApplicationStatusChangedEvent.class);

        for (Method listener : new Method[] { onCreated, onStatusChanged }) {
            TransactionalEventListener annotation = listener.getAnnotation(TransactionalEventListener.class);
            assertNotNull(annotation, listener.getName() + " debe ser @TransactionalEventListener");
            assertEquals(TransactionPhase.AFTER_COMMIT, annotation.phase(),
                    listener.getName() + " debe correr en AFTER_COMMIT");
        }
    }
}
