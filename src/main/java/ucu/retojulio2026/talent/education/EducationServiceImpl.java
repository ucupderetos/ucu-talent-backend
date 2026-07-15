package ucu.retojulio2026.talent.education;

import org.springframework.stereotype.Service;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;

import java.util.List;

@Service
public class EducationServiceImpl implements EducationService {

    private final EducationRepository educationRepository;

    public EducationServiceImpl(EducationRepository educationRepository) {
        this.educationRepository = educationRepository;
    }

    @Override
    public Education create(Education education) {
        education.setId(null);
        return educationRepository.save(education);
    }

    @Override
    public Education getById(String id) {
        return educationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                        "Education con id '" + id + "' no encontrada"));
    }

    @Override
    public List<Education> getByPerfilAlumnoId(String perfilAlumnoId) {
        return educationRepository.getByPerfilAlumnoId(perfilAlumnoId);
    }

    @Override
    public Education update(String id, Education education) {
        Education existing = getById(id);

        existing.setPerfilAlumnoId(education.getPerfilAlumnoId());
        existing.setTitulo(education.getTitulo());
        existing.setCarrera(education.getCarrera());
        existing.setDescripcion(education.getDescripcion());
        existing.setFechaInicio(education.getFechaInicio());
        existing.setFechaFin(education.getFechaFin());

        return educationRepository.save(existing);
    }

    @Override
    public void delete(String id) {
        Education existing = getById(id);
        educationRepository.delete(existing);
    }
}
