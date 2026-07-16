package ucu.retojulio2026.talent.workexperience;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class WorkExperienceServiceImpl implements WorkExperienceService {

    private final WorkExperienceRepository workExperienceRepository;

    public WorkExperienceServiceImpl(WorkExperienceRepository workExperienceRepository) {
        this.workExperienceRepository = workExperienceRepository;
    }

    @Override
    public WorkExperience create(WorkExperience workExperience) {
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
    public WorkExperience update(String id, WorkExperience workExperience) {
        WorkExperience existing = getById(id);

        existing.setStudentProfileId(workExperience.getStudentProfileId());
        existing.setCompany(workExperience.getCompany());
        existing.setPosition(workExperience.getPosition());
        existing.setStartDate(workExperience.getStartDate());
        existing.setEndDate(workExperience.getEndDate());
        existing.setDescription(workExperience.getDescription());

        return workExperienceRepository.save(existing);
    }

    @Override
    public void delete(String id) {
        WorkExperience existing = getById(id);
        workExperienceRepository.delete(existing);
    }
}
