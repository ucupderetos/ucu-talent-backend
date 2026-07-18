package ucu.retojulio2026.talent.area;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ucu.retojulio2026.talent.area.dto.CreateAreaRequest;
import ucu.retojulio2026.talent.area.dto.UpdateAreaRequest;
import ucu.retojulio2026.talent.area.dto.AreaMapper;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;

import java.util.List;

@Service
public class AreaServiceImpl implements AreaService {

    private final AreaRepository areaRepository;
    private final AreaMapper areaMapper;

    public AreaServiceImpl(AreaRepository areaRepository, AreaMapper areaMapper) {
        this.areaRepository = areaRepository;
        this.areaMapper = areaMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Area> getAll() {
        return areaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Area getById(String id) {
        return areaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Area con id '" + id + "' no encontrada"));
    }

    @Override
    @Transactional
    public Area create(CreateAreaRequest request) {
        if (request.parentAreaId() != null && !areaRepository.existsById(request.parentAreaId())) {
            throw new ResourceNotFoundException("Area padre con id '" + request.parentAreaId() + "' no encontrada");
        }
        Area area = areaMapper.toEntity(request);
        return areaRepository.save(area);
    }

    @Override
    @Transactional
    public Area update(String id, UpdateAreaRequest request) {
        Area area = getById(id);

        if (request.parentAreaId() != null && !areaRepository.existsById(request.parentAreaId())) {
            throw new ResourceNotFoundException("Area padre con id '" + request.parentAreaId() + "' no encontrada");
        }

        area.setName(request.name());
        area.setParentAreaId(request.parentAreaId());
        return areaRepository.save(area);
    }

    @Override
    @Transactional
    public void delete(String id) {
        if (!areaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Area con id '" + id + "' no encontrada");
        }
        areaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return areaRepository.existsById(id);
    }
}