package ucu.retojulio2026.talent.vacancy;

import java.util.List;

import org.springframework.stereotype.Component;

import ucu.retojulio2026.talent.company.Company;
import ucu.retojulio2026.talent.company.CompanyService;
import ucu.retojulio2026.talent.mail.MailService;
import ucu.retojulio2026.talent.studentprofile.StudentProfile;
import ucu.retojulio2026.talent.studentprofile.StudentProfileService;
import ucu.retojulio2026.talent.user.User;
import ucu.retojulio2026.talent.user.UserService;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplication;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationRepository;

@Component
public class VacancyFinalizationNotifier {

    private final VacancyApplicationRepository vacancyApplicationRepository;
    private final StudentProfileService studentProfileService;
    private final UserService userService;
    private final CompanyService companyService;
    private final MailService mailService;

    public VacancyFinalizationNotifier(VacancyApplicationRepository vacancyApplicationRepository,
                                        StudentProfileService studentProfileService,
                                        UserService userService,
                                        CompanyService companyService,
                                        MailService mailService) {
        this.vacancyApplicationRepository = vacancyApplicationRepository;
        this.studentProfileService = studentProfileService;
        this.userService = userService;
        this.companyService = companyService;
        this.mailService = mailService;
    }

    public void notifyApplicants(Vacancy vacancy) {
        List<VacancyApplication> applications = vacancyApplicationRepository.findByVacancyId(vacancy.getVacancyId());
        if (applications.isEmpty()) {
            return;
        }

        Company company = companyService.getById(vacancy.getCompanyId());

        for (VacancyApplication application : applications) {
            StudentProfile student = studentProfileService.getById(application.getStudentProfileId());
            User studentUser = userService.getById(application.getStudentProfileId());
            String studentFullName = student.getName() + " " + student.getSurname();

            if (application.isAccepted()) {
                mailService.sendVacancySelectedEmail(
                        studentUser.getEmail(), studentFullName, vacancy.getName(), company.getName());
            } else {
                mailService.sendVacancyClosedEmail(
                        studentUser.getEmail(), studentFullName, vacancy.getName());
            }
        }
    }
}
