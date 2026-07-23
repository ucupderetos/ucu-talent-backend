package ucu.retojulio2026.talent.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ucu.retojulio2026.talent.common.UruguayClock;
import ucu.retojulio2026.talent.company.CompanyDeletionService;
import ucu.retojulio2026.talent.company.CompanyService;
import ucu.retojulio2026.talent.education.Education;
import ucu.retojulio2026.talent.education.EducationService;
import ucu.retojulio2026.talent.studentprofile.StudentProfileService;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplication;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationService;
import ucu.retojulio2026.talent.workexperience.WorkExperience;
import ucu.retojulio2026.talent.workexperience.WorkExperienceService;

import java.time.LocalDateTime;

@Service
public class AccountFacadeImpl implements AccountFacade {

    private final UserService userService;
    private final StudentProfileService studentProfileService;
    private final CompanyService companyService;
    private final CompanyDeletionService companyDeletionService;
    private final EducationService educationService;
    private final WorkExperienceService workExperienceService;
    private final VacancyApplicationService vacancyApplicationService;

    public AccountFacadeImpl(UserService userService, StudentProfileService studentProfileService,
            CompanyService companyService, CompanyDeletionService companyDeletionService,
            EducationService educationService, WorkExperienceService workExperienceService,
            VacancyApplicationService vacancyApplicationService) {
        this.userService = userService;
        this.studentProfileService = studentProfileService;
        this.companyService = companyService;
        this.companyDeletionService = companyDeletionService;
        this.educationService = educationService;
        this.workExperienceService = workExperienceService;
        this.vacancyApplicationService = vacancyApplicationService;
    }

    @Override
    @Transactional
    public void deleteAccount(String userId) {
        User user = userService.getById(userId); // 404 si no existe

        switch (user.getRole()) {
            case ALUMNO -> deleteStudentProfileCascade(userId);
            case EMPRESA -> deleteCompanyCascade(userId);
            case ADMIN -> {
                break;
            }
        }

        userService.delete(userId);
    }

    @Override
    @Transactional
    public void reviewAccount(String userId, AccountStatus status, String adminComment) {
        User user = userService.getById(userId);
        userService.updateStatus(userId, status);

        LocalDateTime reviewedAt = UruguayClock.ahora();
        switch (user.getRole()) {
            case ALUMNO -> studentProfileService.review(userId, reviewedAt, adminComment);
            case EMPRESA -> companyService.review(userId, reviewedAt, adminComment);
            case ADMIN -> {
                break;
            }
        }
    }

    private void deleteStudentProfileCascade(String studentProfileId) {
        if (!studentProfileService.existsById(studentProfileId)) {
            return;
        }
        for (Education education : educationService.getByStudentProfileId(studentProfileId)) {
            educationService.delete(education.getEducationId());
        }
        for (WorkExperience workExperience : workExperienceService.getByStudentProfileId(studentProfileId)) {
            workExperienceService.delete(workExperience.getWorkExperienceId());
        }
        for (VacancyApplication application : vacancyApplicationService.getByStudentProfileId(studentProfileId)) {
            vacancyApplicationService.delete(application.getVacancyApplicationId());
        }
        studentProfileService.delete(studentProfileId);
    }

    private void deleteCompanyCascade(String companyId) {
        if (!companyService.existsById(companyId)) {
            return;
        }
        companyDeletionService.delete(companyId);
    }
}
