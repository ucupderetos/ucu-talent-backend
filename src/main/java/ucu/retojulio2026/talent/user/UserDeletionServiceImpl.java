package ucu.retojulio2026.talent.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ucu.retojulio2026.talent.company.CompanyDeletionService;
import ucu.retojulio2026.talent.company.CompanyService;
import ucu.retojulio2026.talent.education.Education;
import ucu.retojulio2026.talent.education.EducationService;
import ucu.retojulio2026.talent.studentprofile.StudentProfileService;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplication;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationService;
import ucu.retojulio2026.talent.workexperience.WorkExperience;
import ucu.retojulio2026.talent.workexperience.WorkExperienceService;


@Service
public class UserDeletionServiceImpl implements UserDeletionService {

    private final UserService userService;
    private final StudentProfileService studentProfileService;
    private final CompanyService companyService;
    private final CompanyDeletionService companyDeletionService;
    private final EducationService educationService;
    private final WorkExperienceService workExperienceService;
    private final VacancyApplicationService vacancyApplicationService;

    public UserDeletionServiceImpl(UserService userService, StudentProfileService studentProfileService,
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
    public void delete(String userId) {
        User user = userService.getById(userId); // 404 si no existe

        switch (user.getRole()) {
            case ALUMNO -> deleteStudentProfileCascade(userId);
            case EMPRESA -> deleteCompanyCascade(userId);
            case ADMIN -> {
                // ADMIN no tiene perfil asociado.
            }
        }

        userService.delete(userId);
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
