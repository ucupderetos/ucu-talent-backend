package ucu.retojulio2026.talent.Vacancy;

import java.util.List;

public interface IVacancyService {

    List<Vacancy> getAllVacancies();

    Vacancy getVacancyById(Long id);

    Vacancy saveVacancy(Vacancy vacancy);

    Vacancy updateVacancy(Long id, Vacancy vacancy);

    void deleteVacancy(Long id);
}
