package ucu.retojulio2026.talent.vacancyapplication;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import ucu.retojulio2026.talent.mail.MailService;
import ucu.retojulio2026.talent.studentprofile.StudentProfile;
import ucu.retojulio2026.talent.studentprofile.StudentProfileService;
import ucu.retojulio2026.talent.user.User;
import ucu.retojulio2026.talent.user.UserService;
import ucu.retojulio2026.talent.vacancy.Vacancy;
import ucu.retojulio2026.talent.vacancy.VacancyService;

@Component
public class VacancyApplicationMailNotifier {

    private final VacancyService vacancyService;
    private final StudentProfileService studentProfileService;
    private final UserService userService;
    private final MailService mailService;

    public VacancyApplicationMailNotifier(VacancyService vacancyService,
                                          StudentProfileService studentProfileService,
                                          UserService userService,
                                          MailService mailService) {
        this.vacancyService = vacancyService;
        this.studentProfileService = studentProfileService;
        this.userService = userService;
        this.mailService = mailService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApplicationCreated(VacancyApplicationCreatedEvent event) {
        Vacancy vacancy = vacancyService.getVacancyById(event.vacancyId());
        StudentProfile applicant = studentProfileService.getById(event.studentProfileId());
        User companyUser = userService.getById(vacancy.getCompanyId());

        mailService.sendCompanyNewApplicationEmail(
                companyUser.getEmail(), fullNameOf(applicant), vacancy.getName());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApplicationStatusChanged(VacancyApplicationStatusChangedEvent event) {
        Vacancy vacancy = vacancyService.getVacancyById(event.vacancyId());
        StudentProfile applicant = studentProfileService.getById(event.studentProfileId());
        User applicantUser = userService.getById(event.studentProfileId());
        String applicantFullName = fullNameOf(applicant);

        if (event.newStatus() == VacancyApplicationStatus.VISTO) {
            mailService.sendApplicationVistoEmail(
                    applicantUser.getEmail(), applicantFullName, vacancy.getName());
        } else if (event.newStatus() == VacancyApplicationStatus.FINALIZADO) {
            mailService.sendVacancyClosedEmail(
                    applicantUser.getEmail(), applicantFullName, vacancy.getName());
        }
    }

    private static String fullNameOf(StudentProfile applicant) {
        return applicant.getName() + " " + applicant.getSurname();
    }
}
