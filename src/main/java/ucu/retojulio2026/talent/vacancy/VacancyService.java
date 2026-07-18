package ucu.retojulio2026.talent.vacancy;

import ucu.retojulio2026.talent.vacancy.dto.CreateVacancyRequest;

import java.util.List;

public interface VacancyService {

    List<Vacancy> getAllVacancies();

    Vacancy getVacancyById(String id);

    public Vacancy create(CreateVacancyRequest request);

    Vacancy updateVacancy(String id, CreateVacancyRequest vacancy);

    void deleteVacancy(String id);

    boolean existsById(String id);
}
