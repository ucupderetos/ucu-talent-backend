package ucu.retojulio2026.talent.vacancy.filter;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import ucu.retojulio2026.talent.vacancy.Vacancy;
import ucu.retojulio2026.talent.vacancy.VacancyStatus;
import ucu.retojulio2026.talent.vacancy.dto.SearchCriteriaVacancyRequest;

/**
 * Acepta N estrategias que se combinan todas juntas con AND. Spring inyecta
 * automaticamente la lista completa de beans que implementan a la interfaz
 * VacancyFilterStrategy -- agregar un filtro nuevo a futuro (ej:
 * salaryRange) es sumar una clase mas a vacancy/filter/strategy, sin
 * tocar este resolver ni el service.
 */
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

        // Specification.allOf ya devuelve unrestricted() si la lista viene
        // vacia (ningun filtro aplico), no hace falta un chequeo manual.
        return Specification.allOf(aplicables);
    }

    // Fuerza estado PUBLICADO
    public Specification<Vacancy> buildStudentSpecification(SearchCriteriaVacancyRequest criteria) {
        Specification<Vacancy> base = buildSpecification(criteria);
        Specification<Vacancy> soloPublicado =
                (root, query, cb) -> cb.equal(root.get("status"), VacancyStatus.PUBLICADO);
        return base.and(soloPublicado);
    }
}
