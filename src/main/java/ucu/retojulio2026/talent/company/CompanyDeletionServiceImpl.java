package ucu.retojulio2026.talent.company;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.vacancy.Vacancy;
import ucu.retojulio2026.talent.vacancy.VacancyService;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplication;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationService;


@Service
public class CompanyDeletionServiceImpl implements CompanyDeletionService {

    private final CompanyService companyService;
    private final VacancyService vacancyService;
    private final VacancyApplicationService vacancyApplicationService;

    public CompanyDeletionServiceImpl(CompanyService companyService, VacancyService vacancyService,
            VacancyApplicationService vacancyApplicationService) {
        this.companyService = companyService;
        this.vacancyService = vacancyService;
        this.vacancyApplicationService = vacancyApplicationService;
    }

    @Override
    @Transactional
    public void delete(String companyId) {
        if (!companyService.existsById(companyId)) {
            throw new ResourceNotFoundException("Company con id '" + companyId + "' no encontrada");
        }
        for (Vacancy vacancy : vacancyService.getByCompanyId(companyId)) {
            for (VacancyApplication application : vacancyApplicationService.getByVacancyId(vacancy.getVacancyId())) {
                vacancyApplicationService.delete(application.getVacancyApplicationId());
            }
            vacancyService.deleteVacancy(vacancy.getVacancyId());
        }
        companyService.delete(companyId);
    }
}
