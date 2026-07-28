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
import ucu.retojulio2026.talent.degree.Degree;
import ucu.retojulio2026.talent.degree.DegreeRepository;
import ucu.retojulio2026.talent.vacancy.dto.SearchCriteriaVacancyRequest;
import ucu.retojulio2026.talent.vacancy.filter.strategy.CareerVacancyFilterStrategy;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CareerVacancyFilterStrategyTest {

    @Mock private DegreeRepository degreeRepository;
    @Mock private AreaHierarchyService areaHierarchyService;
    @InjectMocks private CareerVacancyFilterStrategy strategy;

    private SearchCriteriaVacancyRequest criteriaConDegree(String degreeId) {
        return new SearchCriteriaVacancyRequest(null, degreeId, null, null, null, null, null, null);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void resolve_degreeIdVacio_devuelveEmpty(String degreeId) {
        var result = strategy.resolve(criteriaConDegree(degreeId));

        assertTrue(result.isEmpty());
        verifyNoInteractions(degreeRepository, areaHierarchyService);
    }

    @Test
    void resolve_carreraInexistente_devuelvePresenteYNoConsultaJerarquia() {
        String degreeId = "deg12345678";
        when(degreeRepository.findById(degreeId)).thenReturn(Optional.empty());

        var result = strategy.resolve(criteriaConDegree(degreeId));

        assertTrue(result.isPresent());
        verify(degreeRepository).findById(degreeId);
        verifyNoInteractions(areaHierarchyService);
    }

    @Test
    void resolve_carreraExistente_resuelveViaElAreaDelDegree() {
        String degreeId = "deg12345678";
        String areaIdDelDegree = "area99999999";

        Degree degree = mock(Degree.class);
        when(degree.getAreaId()).thenReturn(areaIdDelDegree);
        when(degreeRepository.findById(degreeId)).thenReturn(Optional.of(degree));
        when(areaHierarchyService.resolveWithSubareas(areaIdDelDegree))
                .thenReturn(Set.of(areaIdDelDegree, "sub1"));

        var result = strategy.resolve(criteriaConDegree(degreeId));

        assertTrue(result.isPresent());
        verify(areaHierarchyService).resolveWithSubareas(areaIdDelDegree);
    }
}