package ucu.retojulio2026.talent.education;

import org.springframework.stereotype.Service;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.degree.DegreeRepository;
import ucu.retojulio2026.talent.education.dto.CreateEducationRequest;
import ucu.retojulio2026.talent.education.dto.EducationMapper;
import ucu.retojulio2026.talent.education.dto.UpdateEducationRequest;
import ucu.retojulio2026.talent.studentprofile.StudentProfileService;

import java.util.List;

@Service
public class EducationServiceImpl implements EducationService {

    private final EducationRepository educationRepository;
    private final EducationMapper educationMapper;
    private final StudentProfileService studentProfileService;
    private final DegreeRepository degreeRepository;

    public EducationServiceImpl(EducationRepository educationRepository,
                                EducationMapper educationMapper,
                                StudentProfileService studentProfileService,
                                DegreeRepository degreeRepository) {
        this.educationRepository = educationRepository;
        this.educationMapper = educationMapper;
        this.studentProfileService = studentProfileService;
        this.degreeRepository = degreeRepository;
    }

    @Override
    public Education create(CreateEducationRequest request) {
        validateRelatedIds(request.studentProfileId(), request.degreeId());

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
        validateRelatedIds(request.studentProfileId(), request.degreeId());

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

    private void validateRelatedIds(String studentProfileId, String degreeId) {
        if (!studentProfileService.existsById(studentProfileId)) {
            throw new ResourceNotFoundException("StudentProfile con id '" + studentProfileId + "' no encontrado");
        }

        if (!degreeRepository.existsById(degreeId)) {
            throw new ResourceNotFoundException("Degree con id '" + degreeId + "' no encontrado");
        }
    }
}
