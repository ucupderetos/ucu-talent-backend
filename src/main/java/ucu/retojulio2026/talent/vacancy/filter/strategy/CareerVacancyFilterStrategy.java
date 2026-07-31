package ucu.retojulio2026.talent.vacancy.filter.strategy;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import ucu.retojulio2026.talent.area.AreaHierarchyService;
import ucu.retojulio2026.talent.degree.Degree;
import ucu.retojulio2026.talent.degree.DegreeRepository;
import ucu.retojulio2026.talent.vacancy.Vacancy;
import ucu.retojulio2026.talent.vacancy.filter.VacancyFilterStrategy;
import ucu.retojulio2026.talent.vacancy.dto.SearchCriteriaVacancyRequest;

@Component
public class CareerVacancyFilterStrategy implements VacancyFilterStrategy {

    private final DegreeRepository degreeRepository;
    private final AreaHierarchyService areaHierarchyService;

    public CareerVacancyFilterStrategy(DegreeRepository degreeRepository,
                                       AreaHierarchyService areaHierarchyService) {
        this.degreeRepository = degreeRepository;
        this.areaHierarchyService = areaHierarchyService;
    }

    @Override
    public Optional<Specification<Vacancy>> resolve(SearchCriteriaVacancyRequest criteria) {
        if (criteria.degreeId() == null || criteria.degreeId().isBlank()) {
            return Optional.empty();
        }
        Optional<Degree> degree = degreeRepository.findById(criteria.degreeId());
        if (degree.isEmpty()) {
            return Optional.of((root, query, cb) -> cb.disjunction());
        }
        Set<String> areaIds = areaHierarchyService.resolveWithSubareas(degree.get().getAreaId());
        return Optional.of((root, query, cb) -> root.get("areaId").in(areaIds));
    }
}
