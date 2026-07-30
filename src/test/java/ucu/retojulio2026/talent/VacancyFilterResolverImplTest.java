package ucu.retojulio2026.talent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.jpa.domain.Specification;

import ucu.retojulio2026.talent.vacancy.Vacancy;
import ucu.retojulio2026.talent.vacancy.dto.SearchCriteriaVacancyRequest;
import ucu.retojulio2026.talent.vacancy.filter.VacancyFilterResolverImpl;
import ucu.retojulio2026.talent.vacancy.filter.VacancyFilterStrategy;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VacancyFilterResolverImplTest {

    private SearchCriteriaVacancyRequest criteriosVacios() {
        return new SearchCriteriaVacancyRequest(null, null, null, null, null, null, null, null);
    }

    @Test
    void buildSpecification_criteriosNull_devuelveSpecNoNula() {
        VacancyFilterResolverImpl resolver = new VacancyFilterResolverImpl(List.of());

        Specification<Vacancy> spec = resolver.buildSpecification(criteriosVacios());

        assertNotNull(spec);
    }

    @Test
    void buildSpecification_combinaSoloLasEstrategiasAplicables() {
        SearchCriteriaVacancyRequest criteria = criteriosVacios();

        VacancyFilterStrategy noAplica = mock(VacancyFilterStrategy.class);
        VacancyFilterStrategy aplica1 = mock(VacancyFilterStrategy.class);
        VacancyFilterStrategy aplica2 = mock(VacancyFilterStrategy.class);

        Specification<Vacancy> spec1 = (root, query, cb) -> null;
        Specification<Vacancy> spec2 = (root, query, cb) -> null;

        when(noAplica.resolve(criteria)).thenReturn(Optional.empty());
        when(aplica1.resolve(criteria)).thenReturn(Optional.of(spec1));
        when(aplica2.resolve(criteria)).thenReturn(Optional.of(spec2));

        VacancyFilterResolverImpl resolver =
                new VacancyFilterResolverImpl(List.of(noAplica, aplica1, aplica2));

        Specification<Vacancy> result = resolver.buildSpecification(criteria);

        assertNotNull(result);
        verify(noAplica).resolve(criteria);
        verify(aplica1).resolve(criteria);
        verify(aplica2).resolve(criteria);
    }

    @Test
    void buildStudentSpecification_agregaFiltroSobreLaBase() {
        VacancyFilterResolverImpl resolver = new VacancyFilterResolverImpl(List.of());
        SearchCriteriaVacancyRequest criteria = criteriosVacios();

        Specification<Vacancy> base = resolver.buildSpecification(criteria);
        Specification<Vacancy> studentSpec = resolver.buildStudentSpecification(criteria);

        assertNotNull(studentSpec);
        assertNotSame(base, studentSpec);
    }
}