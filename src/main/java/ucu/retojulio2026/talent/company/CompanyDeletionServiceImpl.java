package ucu.retojulio2026.talent.company;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.vacancy.Vacancy;
import ucu.retojulio2026.talent.vacancy.VacancyService;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplication;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationService;

// Orquesta el borrado en cascada de Company, mismo motivo que UserDeletionService: VacancyService
// ya depende de CompanyService (para chequear Company.approved), asi que si CompanyServiceImpl
// dependiera de VacancyService para esto se forma un ciclo de beans. Este servicio depende de los
// 3 y ninguno de ellos depende de este, asi que no hay ciclo. UserDeletionServiceImpl reusa este
// servicio en vez de duplicar la logica de cascada.
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
