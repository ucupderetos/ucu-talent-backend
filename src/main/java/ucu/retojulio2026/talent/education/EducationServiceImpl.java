package ucu.retojulio2026.talent.education;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.degree.DegreeRepository;
import ucu.retojulio2026.talent.degree.Degree;
import ucu.retojulio2026.talent.education.dto.CreateEducationRequest;
import ucu.retojulio2026.talent.education.dto.EducationMapper;
import ucu.retojulio2026.talent.education.dto.UpdateEducationRequest;
import ucu.retojulio2026.talent.studentprofile.StudentProfileService;

import java.util.List;
import java.time.LocalDate;

@Service
public class EducationServiceImpl implements EducationService {

    private static final LocalDate MIN_LOGICAL_START_DATE = LocalDate.of(1900, 1, 1);

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
        validateRelatedData(request.studentProfileId(), request.degreeId(), request.institution());
        validateDateRange(request.startDate(), request.endDate());
        validateLogicalDateRange(request.startDate());

        Education education = educationMapper.toEntity(request);
        return educationRepository.save(education);
    }

    @Override
    public List<Education> getAll() {
        return educationRepository.findAll();
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
        validateRelatedData(request.studentProfileId(), request.degreeId(), request.institution());
        validateDateRange(request.startDate(), request.endDate());
        validateLogicalDateRange(request.startDate());

        existing.setStudentProfileId(request.studentProfileId());
        existing.setDegreeLevel(request.degreeLevel());
        existing.setDegreeId(request.degreeId());
        existing.setInstitution(request.institution());
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

    private void validateRelatedData(String studentProfileId, String degreeId, String institution) {
        if (!studentProfileService.existsById(studentProfileId)) {
            throw new ResourceNotFoundException("StudentProfile con id '" + studentProfileId + "' no encontrado");
        }

        Degree degree = degreeRepository.findById(degreeId)
                .orElseThrow(() -> new ResourceNotFoundException("Degree con id '" + degreeId + "' no encontrado"));

        if (Boolean.FALSE.equals(degree.getIsUcu()) && (institution == null || institution.isBlank())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La institucion es obligatoria cuando la carrera no es de UCU"
            );
        }
    }

    private void validateDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha de fin no puede ser anterior a la fecha de inicio"
            );
        }
    }

    private void validateLogicalDateRange(LocalDate startDate) {
        if (startDate.isBefore(MIN_LOGICAL_START_DATE)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha de inicio no puede ser anterior al 01/01/1900"
            );
        }
    }
}
