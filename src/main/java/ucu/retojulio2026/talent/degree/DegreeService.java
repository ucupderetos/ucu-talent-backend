package ucu.retojulio2026.talent.degree;

import java.util.List;

import ucu.retojulio2026.talent.degree.dto.CreateDegreeRequest;
import ucu.retojulio2026.talent.degree.dto.UpdateDegreeRequest;

public interface DegreeService {

    Degree create(CreateDegreeRequest request);

    Degree getById(String id);

    Degree getByName(String name);

    List<Degree> getAll();

    List<Degree> getByAreaId(String areaId);

    Degree update(String id, UpdateDegreeRequest request);

    void delete(String id);
}