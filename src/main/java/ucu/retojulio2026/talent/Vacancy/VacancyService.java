package ucu.retojulio2026.talent.Vacancy;

import ucu.retojulio2026.talent.Vacancy.dto.CreateVacancyRequest;
import ucu.retojulio2026.talent.Vacancy.dto.VacancyResponse;

import java.util.List;

public interface VacancyService {

    List<Vacancy> getAllVacancies();

    Vacancy getVacancyById(String id);

    public Vacancy create(CreateVacancyRequest request);

    Vacancy updateVacancy(String id, CreateVacancyRequest vacancy);

    void deleteVacancy(String id);
}
