package ucu.retojulio2026.talent.vacancy.filter.strategy;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import ucu.retojulio2026.talent.vacancy.Vacancy;
import ucu.retojulio2026.talent.vacancy.dto.SearchCriteriaVacancyRequest;
import ucu.retojulio2026.talent.vacancy.filter.VacancyFilterStrategy;

import java.util.Optional;

@Component
public class DeletedVacancyFilterStrategy implements VacancyFilterStrategy {

    @Override
    public Optional<Specification<Vacancy>> resolve(SearchCriteriaVacancyRequest criteria) {
        if (criteria.deleted() == null) {
            return Optional.empty();
        }
        return Optional.of((root, query, cb) -> cb.equal(root.get("deleted"), criteria.deleted()));
    }
}