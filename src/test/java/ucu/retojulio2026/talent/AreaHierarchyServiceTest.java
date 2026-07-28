package ucu.retojulio2026.talent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ucu.retojulio2026.talent.area.Area;
import ucu.retojulio2026.talent.area.AreaHierarchyService;
import ucu.retojulio2026.talent.area.AreaRepository;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AreaHierarchyServiceTest {

    @Mock private AreaRepository areaRepository;
    @InjectMocks private AreaHierarchyService service;

    private Area area(String id) {
        Area a = new Area();
        a.setAreaId(id);
        return a;
    }

    @Test
    void resolveWithSubareas_areaHoja_devuelveSoloEsaArea() {
        when(areaRepository.findByParentAreaId("area1")).thenReturn(List.of());

        Set<String> result = service.resolveWithSubareas("area1");

        assertEquals(Set.of("area1"), result);
    }

    @Test
    void resolveWithSubareas_arbolMultinivel_devuelveTodosLosDescendientes() {
        when(areaRepository.findByParentAreaId("area1"))
                .thenReturn(List.of(area("area2"), area("area3")));
        when(areaRepository.findByParentAreaId("area2"))
                .thenReturn(List.of(area("area4")));

        Set<String> result = service.resolveWithSubareas("area1");

        assertEquals(Set.of("area1", "area2", "area3", "area4"), result);
    }

    @Test
    @Timeout(2)
    void resolveWithSubareas_conCiclo_noEntraEnLoopInfinito() {
        when(areaRepository.findByParentAreaId("A")).thenReturn(List.of(area("B")));
        when(areaRepository.findByParentAreaId("B")).thenReturn(List.of(area("A")));

        Set<String> result = service.resolveWithSubareas("A");

        assertEquals(Set.of("A", "B"), result);
    }
}