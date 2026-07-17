package ucu.retojulio2026.talent.vacancyapplication;

public interface VacancyApplicationService {

    VacancyApplication create(VacancyApplication vacancyApplication);

    VacancyApplication getById(String id);

    VacancyApplication update(String id, VacancyApplicationStatus status);

    void delete(String id);
}
