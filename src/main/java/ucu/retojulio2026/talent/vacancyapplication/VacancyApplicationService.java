package ucu.retojulio2026.talent.vacancyapplication;

import ucu.retojulio2026.talent.vacancyapplication.dto.CreateVacancyApplicationRequest;

import java.util.List;

public interface VacancyApplicationService {

    VacancyApplication create(CreateVacancyApplicationRequest vacancyApplication);

    VacancyApplication getById(String id);

    List<VacancyApplication> getAll();

    List<VacancyApplication> getByVacancyId(String vacancyId);

    List<VacancyApplication> getByStudentProfileId(String studentProfileId);

    List<VacancyApplication> getByStatus(VacancyApplicationStatus status);

    VacancyApplication update(String id, VacancyApplicationStatus status);

    void delete(String id);
}
