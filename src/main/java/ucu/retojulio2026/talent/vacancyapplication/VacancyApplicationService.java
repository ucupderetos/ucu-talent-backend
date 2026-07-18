package ucu.retojulio2026.talent.vacancyapplication;

import ucu.retojulio2026.talent.vacancyapplication.dto.CreateVacancyApplicationRequest;

public interface VacancyApplicationService {

    VacancyApplication create(CreateVacancyApplicationRequest vacancyApplication);

    VacancyApplication getById(String id);

    VacancyApplication update(String id, VacancyApplicationStatus status);

    void delete(String id);
}
