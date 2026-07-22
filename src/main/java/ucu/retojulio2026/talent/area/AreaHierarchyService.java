package ucu.retojulio2026.talent.area;

import java.util.*;

import org.springframework.stereotype.Service;

/**
 * Filtro por area tiene que incluir tambien sus subareas. Area
 * es un arbol (self-reference via parentAreaId), asi que la expansion es
 * un recorrido BFS desde el area pedida hacia abajo.
 *
 * Usado tanto por el filtro de area (vacancy/filter) como por el de
 * carrera (una carrera se resuelve via su area, y esa area
 * tambien incluye sus subareas).
 */
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