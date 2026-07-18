package ucu.retojulio2026.talent.area;

import ucu.retojulio2026.talent.area.dto.CreateAreaRequest;
import ucu.retojulio2026.talent.area.dto.UpdateAreaRequest;

import java.util.List;

public interface AreaService {

    List<Area> getAll();

    Area getById(String id);

    Area create(CreateAreaRequest request);

    Area update(String id, UpdateAreaRequest request);

    void delete(String id);

    boolean existsById(String id);
}