package ucu.retojulio2026.talent.degree;

import java.util.List;

import org.springframework.stereotype.Service;

import ucu.retojulio2026.talent.degree.dto.CreateDegreeRequest;
import ucu.retojulio2026.talent.degree.dto.UpdateDegreeRequest;
import ucu.retojulio2026.talent.degree.dto.DegreeMapper;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;

//Implementacion concreta del contrato DegreeService. Es el bean que Spring inyecta.
@Service
public class DegreeServiceImpl implements DegreeService {

    private final DegreeRepository degreeRepository;
    private final DegreeMapper degreeMapper;

    public DegreeServiceImpl(DegreeRepository degreeRepository, DegreeMapper degreeMapper) {
        this.degreeRepository = degreeRepository;
        this.degreeMapper = degreeMapper;
    }

    @Override
    public Degree create(CreateDegreeRequest request) {
        Degree degree = degreeMapper.toEntity(request);
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
    public Degree getByName(String name) {
        return degreeRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Degree con nombre '" + name + "' no encontrado"));
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
        degree.setName(request.name());
        degree.setIsUcu(request.isUcu());
        return degreeRepository.save(degree);

        // No se modifica degreeId porque es la clave primaria de la entidad.
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