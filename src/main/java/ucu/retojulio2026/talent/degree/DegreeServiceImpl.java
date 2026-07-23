package ucu.retojulio2026.talent.degree;

import java.util.List;

import org.springframework.stereotype.Service;

import ucu.retojulio2026.talent.area.AreaService;
import ucu.retojulio2026.talent.degree.dto.CreateDegreeRequest;
import ucu.retojulio2026.talent.degree.dto.UpdateDegreeRequest;
import ucu.retojulio2026.talent.degree.dto.DegreeMapper;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;

//Implementacion concreta del contrato DegreeService. Es el bean que Spring inyecta.
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

        // Si no hay carrera, lanza la excepcion. Spring la rutea al GlobalExceptionHandler
        // clase global con @RestControllerAdvice, que la traduce a un 404 en el metodo handleNotFound.
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
        Degree degree = getById(id); // lanza 404 si no existe
        degree.setAreaId(request.areaId());
        degree.setName(normalizeName(request.name()));
        degree.setIsUcu(request.isUcu());
        return degreeRepository.save(degree);

        // No se modifica degreeId porque es la clave primaria de la entidad.
    }

    // Sin esto, "  base de datos" y "Base de datos" quedan como carreras distintas a
    // simple vista aunque sean la misma. Solo capitaliza la primera letra, no toca el resto.
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