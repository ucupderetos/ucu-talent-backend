package ucu.retojulio2026.talent.vacancy.filter;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import ucu.retojulio2026.talent.vacancy.Vacancy;
import ucu.retojulio2026.talent.vacancy.VacancyStatus;
import ucu.retojulio2026.talent.vacancy.dto.SearchCriteriaVacancyRequest;

@Component
public class VacancyFilterResolverImpl {

    private final List<VacancyFilterStrategy> strategies;

    public VacancyFilterResolverImpl(List<VacancyFilterStrategy> strategies) {
        this.strategies = strategies;
    }

    public Specification<Vacancy> buildSpecification(SearchCriteriaVacancyRequest criteria) {
        List<Specification<Vacancy>> aplicables = strategies.stream()
                .map(strategy -> strategy.resolve(criteria))
                .flatMap(Optional::stream)
                .toList();

        return Specification.allOf(aplicables);
    }

    public Specification<Vacancy> buildStudentSpecification(SearchCriteriaVacancyRequest criteria) {
        Specification<Vacancy> base = buildSpecification(criteria);
        Specification<Vacancy> soloPublicado =
                (root, query, cb) -> cb.equal(root.get("status"), VacancyStatus.PUBLICADO);
        return base.and(soloPublicado);
    }
}
