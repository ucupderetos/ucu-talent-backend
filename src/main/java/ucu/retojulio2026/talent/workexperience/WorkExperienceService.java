package ucu.retojulio2026.talent.workexperience;

import ucu.retojulio2026.talent.workexperience.dto.CreateWorkExperienceRequest;
import ucu.retojulio2026.talent.workexperience.dto.UpdateWorkExperienceRequest;

import java.util.List;

public interface WorkExperienceService {

    WorkExperience create(CreateWorkExperienceRequest request);

    WorkExperience getById(String id);

    List<WorkExperience> getByStudentProfileId(String studentProfileId);

    WorkExperience update(String id, UpdateWorkExperienceRequest request);

    void delete(String id);
}
