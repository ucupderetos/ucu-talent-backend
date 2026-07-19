package ucu.retojulio2026.talent.workexperience;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.studentprofile.StudentProfileService;
import ucu.retojulio2026.talent.workexperience.dto.CreateWorkExperienceRequest;
import ucu.retojulio2026.talent.workexperience.dto.UpdateWorkExperienceRequest;
import ucu.retojulio2026.talent.workexperience.dto.WorkExperienceMapper;

import java.util.List;

@Service
public class WorkExperienceServiceImpl implements WorkExperienceService {

    private final WorkExperienceRepository workExperienceRepository;
    private final WorkExperienceMapper workExperienceMapper;
    private final StudentProfileService studentProfileService;

    public WorkExperienceServiceImpl(WorkExperienceRepository workExperienceRepository,
                                     WorkExperienceMapper workExperienceMapper,
                                     StudentProfileService studentProfileService) {
        this.workExperienceRepository = workExperienceRepository;
        this.workExperienceMapper = workExperienceMapper;
        this.studentProfileService = studentProfileService;
    }

    @Override
    public WorkExperience create(CreateWorkExperienceRequest request) {
        validateStudentProfileExists(request.studentProfileId());

        WorkExperience workExperience = workExperienceMapper.toEntity(request);
        // Forzamos creacion: el id siempre lo asigna la entidad en @PrePersist.
        workExperience.setWorkExperienceId(null);
        return workExperienceRepository.save(workExperience);
    }

    @Override
    public WorkExperience getById(String id) {
        return workExperienceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "WorkExperience con id '" + id + "' no encontrado"));
    }

    @Override
    public List<WorkExperience> getByStudentProfileId(String studentProfileId) {
        return workExperienceRepository.findByStudentProfileId(studentProfileId);
    }

    @Override
    public WorkExperience update(String id, UpdateWorkExperienceRequest request) {
        WorkExperience existing = getById(id);
        validateStudentProfileExists(request.studentProfileId());

        existing.setStudentProfileId(request.studentProfileId());
        existing.setCompany(request.company());
        existing.setPosition(request.position());
        existing.setStartDate(request.startDate());
        existing.setEndDate(request.endDate());
        existing.setDescription(request.description());

        return workExperienceRepository.save(existing);
    }

    @Override
    public void delete(String id) {
        WorkExperience existing = getById(id);
        workExperienceRepository.delete(existing);
    }

    private void validateStudentProfileExists(String studentProfileId) {
        if (!studentProfileService.existsById(studentProfileId)) {
            throw new ResourceNotFoundException(
                    "StudentProfile con id '" + studentProfileId + "' no encontrado");
        }
    }
}
