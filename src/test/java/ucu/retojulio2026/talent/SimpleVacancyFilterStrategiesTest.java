package ucu.retojulio2026.talent;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import ucu.retojulio2026.talent.vacancy.dto.SearchCriteriaVacancyRequest;
import ucu.retojulio2026.talent.vacancy.filter.VacancyFilterStrategy;
import ucu.retojulio2026.talent.vacancy.filter.strategy.ContractTypeVacancyFilterStrategy;
import ucu.retojulio2026.talent.vacancy.filter.strategy.DeletedVacancyFilterStrategy;
import ucu.retojulio2026.talent.vacancy.filter.strategy.KeywordVacancyFilterStrategy;
import ucu.retojulio2026.talent.vacancy.filter.strategy.LocationVacancyFilterStrategy;
import ucu.retojulio2026.talent.vacancy.filter.strategy.ModalityVacancyFilterStrategy;
import ucu.retojulio2026.talent.vacancy.filter.strategy.StatusVacancyFilterStrategy;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleVacancyFilterStrategiesTest {

    static Stream<VacancyFilterStrategy> strategiesSimples() {
        return Stream.of(
                new ContractTypeVacancyFilterStrategy(),
                new ModalityVacancyFilterStrategy(),
                new LocationVacancyFilterStrategy(),
                new KeywordVacancyFilterStrategy(),
                new StatusVacancyFilterStrategy(),
                new DeletedVacancyFilterStrategy()
        );
    }

    @ParameterizedTest
    @MethodSource("strategiesSimples")
    void resolve_criterioNull_devuelveEmpty(VacancyFilterStrategy strategy) {
        SearchCriteriaVacancyRequest criteriaVacia =
                new SearchCriteriaVacancyRequest(null, null, null, null, null, null, null, null);

        assertTrue(strategy.resolve(criteriaVacia).isEmpty());
    }
}