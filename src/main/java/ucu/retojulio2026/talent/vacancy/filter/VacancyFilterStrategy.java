package ucu.retojulio2026.talent.vacancy.filter;

import org.springframework.data.jpa.domain.Specification;
import ucu.retojulio2026.talent.vacancy.Vacancy;
import ucu.retojulio2026.talent.vacancy.dto.SearchCriteriaVacancyRequest;

import java.util.Optional;

public interface VacancyFilterStrategy {
    Optional<Specification<Vacancy>> resolve(SearchCriteriaVacancyRequest criteria);
}
