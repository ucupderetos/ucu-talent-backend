package ucu.retojulio2026.talent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ucu.retojulio2026.talent.area.AreaHierarchyService;
import ucu.retojulio2026.talent.vacancy.dto.SearchCriteriaVacancyRequest;
import ucu.retojulio2026.talent.vacancy.filter.strategy.AreaVacancyFilterStrategy;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AreaVacancyFilterStrategyTest {

    @Mock private AreaHierarchyService areaHierarchyService;
    @InjectMocks private AreaVacancyFilterStrategy strategy;

    private SearchCriteriaVacancyRequest criteriaConArea(String areaId) {
        return new SearchCriteriaVacancyRequest(areaId, null, null, null, null, null, null, null);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void resolve_areaIdVacio_devuelveEmptyYNoConsultaJerarquia(String areaId) {
        var result = strategy.resolve(criteriaConArea(areaId));

        assertTrue(result.isEmpty());
        verifyNoInteractions(areaHierarchyService);
    }

    @Test
    void resolve_conAreaId_devuelvePresenteYLlamaResolveWithSubareas() {
        String areaId = "area12345678";
        when(areaHierarchyService.resolveWithSubareas(areaId))
                .thenReturn(Set.of(areaId, "sub1", "sub2"));

        var result = strategy.resolve(criteriaConArea(areaId));

        assertTrue(result.isPresent());
        verify(areaHierarchyService).resolveWithSubareas(areaId);
    }
}