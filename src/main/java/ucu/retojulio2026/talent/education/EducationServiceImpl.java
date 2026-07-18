package ucu.retojulio2026.talent.education;

import org.springframework.stereotype.Service;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.education.dto.CreateEducationRequest;
import ucu.retojulio2026.talent.education.dto.EducationMapper;
import ucu.retojulio2026.talent.education.dto.UpdateEducationRequest;

import java.util.List;

@Service
public class EducationServiceImpl implements EducationService {

    private final EducationRepository educationRepository;
    private final EducationMapper educationMapper;

    public EducationServiceImpl(EducationRepository educationRepository, EducationMapper educationMapper) {
        this.educationRepository = educationRepository;
        this.educationMapper = educationMapper;
    }

    @Override
    public Education create(CreateEducationRequest request) {
        Education education = educationMapper.toEntity(request);
        return educationRepository.save(education);
    }

    @Override
    public Education getByEducationId(String educationId) {
        return educationRepository.findById(educationId)
            .orElseThrow(() -> new ResourceNotFoundException(
                        "Education con id '" + educationId + "' no encontrada"));
    }

    @Override
    public List<Education> getByStudentProfileId(String studentProfileId) {
        return educationRepository.findByStudentProfileId(studentProfileId);
    }

    @Override
    public Education update(String educationId, UpdateEducationRequest request) {
        Education existing = getByEducationId(educationId);

        existing.setStudentProfileId(request.studentProfileId());
        existing.setDegreeLevel(request.degreeLevel());
        existing.setDegreeId(request.degreeId());
        existing.setDescription(request.description());
        existing.setStartDate(request.startDate());
        existing.setEndDate(request.endDate());

        return educationRepository.save(existing);
    }

    @Override
    public void delete(String educationId) {
        Education existing = getByEducationId(educationId);
        educationRepository.delete(existing);
    }
}
