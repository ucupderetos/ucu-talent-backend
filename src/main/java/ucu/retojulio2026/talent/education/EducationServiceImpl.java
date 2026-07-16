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
        education.setEducation_id(null);
        return educationRepository.save(education);
    }

    @Override
    public Education getByEducationId(String education_id) {
        return educationRepository.findById(education_id)
            .orElseThrow(() -> new ResourceNotFoundException(
                        "Education con id '" + education_id + "' no encontrada"));
    }

    @Override
    public List<Education> getByStudentProfileId(String studentProfileId) {
        return educationRepository.findByStudentProfileId(studentProfileId);
    }

    @Override
    public Education update(String education_id, Education education) {
        Education existing = getByEducationId(education_id);

        existing.setStudentProfileId(education.getStudentProfileId());
        existing.setDegreeLevel(education.getDegreeLevel());
        existing.setDegreeId(education.getDegreeId());
        existing.setDescription(education.getDescription());
        existing.setStartDate(education.getStartDate());
        existing.setEndDate(education.getEndDate());

        return educationRepository.save(existing);
    }

    @Override
    public void delete(String education_id) {
        Education existing = getByEducationId(education_id);
        educationRepository.delete(existing);
    }
}
