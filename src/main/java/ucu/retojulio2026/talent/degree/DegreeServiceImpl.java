package ucu.retojulio2026.talent.degree;

import java.util.List;

import org.springframework.stereotype.Service;

import ucu.retojulio2026.talent.area.AreaService;
import ucu.retojulio2026.talent.degree.dto.CreateDegreeRequest;
import ucu.retojulio2026.talent.degree.dto.UpdateDegreeRequest;
import ucu.retojulio2026.talent.degree.dto.DegreeMapper;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;

@Service
public class DegreeServiceImpl implements DegreeService {

    private final DegreeRepository degreeRepository;
    private final DegreeMapper degreeMapper;
    private final AreaService areaService;

    public DegreeServiceImpl(DegreeRepository degreeRepository, DegreeMapper degreeMapper, AreaService areaService) {
        this.degreeRepository = degreeRepository;
        this.degreeMapper = degreeMapper;
        this.areaService = areaService;
    }

    @Override
    public Degree create(CreateDegreeRequest request) {
        areaService.getById(request.areaId());
        Degree degree = degreeMapper.toEntity(request);
        degree.setName(normalizeName(degree.getName()));
        return degreeRepository.save(degree);
    }

    @Override
    public Degree getById(String id) {
        return degreeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Degree con id '" + id + "' no encontrado"));

    }

    @Override
    public List<Degree> getAll() {
        return degreeRepository.findAll();
    }

    @Override
    public List<Degree> getByAreaId(String areaId) {
        return degreeRepository.findByAreaId(areaId);
    }

    @Override
    public Degree update(String id, UpdateDegreeRequest request) {
        Degree degree = getById(id);
        degree.setAreaId(request.areaId());
        degree.setName(normalizeName(request.name()));
        degree.setIsUcu(request.isUcu());
        return degreeRepository.save(degree);

    }

    private String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        return Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
    }

    @Override
    public void delete(String id) {
        if (!degreeRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Degree con id '" + id + "' no encontrado");
        }
        degreeRepository.deleteById(id);
    }
}
