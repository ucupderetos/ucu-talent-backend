package ucu.retojulio2026.talent.area;

import java.util.*;

import org.springframework.stereotype.Service;

@Service
public class AreaHierarchyService {

    private final AreaRepository areaRepository;

    public AreaHierarchyService(AreaRepository areaRepository) {
        this.areaRepository = areaRepository;
    }

    public Set<String> resolveWithSubareas(String areaId) {
        Set<String> visitados = new HashSet<>();
        Deque<String> pendientes = new ArrayDeque<>();
        pendientes.add(areaId);

        while (!pendientes.isEmpty()) {
            String actual = pendientes.poll();
            if (visitados.add(actual)) {
                List<Area> subareas = areaRepository.findByParentAreaId(actual);
                subareas.forEach(subarea ->
                        pendientes.add(subarea.getAreaId()));
            }
        }
        return visitados;
    }
}
