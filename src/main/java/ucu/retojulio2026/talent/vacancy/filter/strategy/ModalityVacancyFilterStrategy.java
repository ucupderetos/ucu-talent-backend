package ucu.retojulio2026.talent.vacancy.filter.strategy;

import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import ucu.retojulio2026.talent.vacancy.Vacancy;
import ucu.retojulio2026.talent.vacancy.filter.VacancyFilterStrategy;
import ucu.retojulio2026.talent.vacancy.dto.SearchCriteriaVacancyRequest;

@Component
public class ModalityVacancyFilterStrategy implements VacancyFilterStrategy {

    @Override
    public Optional<Specification<Vacancy>> resolve(SearchCriteriaVacancyRequest criteria) {
        if (criteria.modality() == null) {
            return Optional.empty();
        }
        return Optional.of((root, query, cb) -> cb.equal(root.get("modality"), criteria.modality()));
    }
}