package ucu.retojulio2026.talent.vacancy.filter.strategy;

import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import ucu.retojulio2026.talent.vacancy.Vacancy;
import ucu.retojulio2026.talent.vacancy.filter.VacancyFilterStrategy;
import ucu.retojulio2026.talent.vacancy.dto.SearchCriteriaVacancyRequest;

/**
 * keyword sobre nombre O descripcion (no ambos a la vez). LIKE
 * case-insensitive, sin acentos/tildes normalizados.
 */
@Component
public class KeywordVacancyFilterStrategy implements VacancyFilterStrategy {

    @Override
    public Optional<Specification<Vacancy>> resolve(SearchCriteriaVacancyRequest criteria) {
        if (criteria.keyword() == null || criteria.keyword().isBlank()) {
            return Optional.empty();
        }
        String patron = "%" + criteria.keyword().trim().toLowerCase() + "%";
        return Optional.of((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), patron),
                cb.like(cb.lower(root.get("description")), patron)
        ));
    }
}