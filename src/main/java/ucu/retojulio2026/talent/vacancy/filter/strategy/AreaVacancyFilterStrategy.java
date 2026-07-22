package ucu.retojulio2026.talent.vacancy.filter.strategy;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import ucu.retojulio2026.talent.area.AreaHierarchyService;
import ucu.retojulio2026.talent.vacancy.Vacancy;
import ucu.retojulio2026.talent.vacancy.filter.VacancyFilterStrategy;
import ucu.retojulio2026.talent.vacancy.dto.SearchCriteriaVacancyRequest;

@Component
public class AreaVacancyFilterStrategy implements VacancyFilterStrategy {

    private final AreaHierarchyService areaHierarchyService;

    public AreaVacancyFilterStrategy(AreaHierarchyService areaHierarchyService) {
        this.areaHierarchyService = areaHierarchyService;
    }

    @Override
    public Optional<Specification<Vacancy>> resolve(SearchCriteriaVacancyRequest criteria) {
        if (criteria.areaId() == null || criteria.areaId().isBlank()) {
            return Optional.empty();
        }
        Set<String> areaIds = areaHierarchyService.resolveWithSubareas(criteria.areaId());
        return Optional.of((root, query, cb) -> root.get("areaId").in(areaIds));
    }
}