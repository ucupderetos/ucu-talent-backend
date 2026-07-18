package ucu.retojulio2026.talent.workexperience;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ucu.retojulio2026.talent.workexperience.dto.CreateWorkExperienceRequest;
import ucu.retojulio2026.talent.workexperience.dto.UpdateWorkExperienceRequest;
import ucu.retojulio2026.talent.workexperience.dto.WorkExperienceMapper;

import java.util.List;

@Service
public class WorkExperienceServiceImpl implements WorkExperienceService {

    private final WorkExperienceRepository workExperienceRepository;
    private final WorkExperienceMapper workExperienceMapper;

    public WorkExperienceServiceImpl(WorkExperienceRepository workExperienceRepository,
                                     WorkExperienceMapper workExperienceMapper) {
        this.workExperienceRepository = workExperienceRepository;
        this.workExperienceMapper = workExperienceMapper;
    }

    @Override
    public WorkExperience create(CreateWorkExperienceRequest request) {
        WorkExperience workExperience = workExperienceMapper.toEntity(request);
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
}
