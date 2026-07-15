package ucu.retojulio2026.talent.workexperience;

import java.util.List;

public interface WorkExperienceService {

    WorkExperience create(WorkExperience workExperience);

    WorkExperience getById(String id);

    List<WorkExperience> getByPerfilAlumnoId(String perfilAlumnoId);

    WorkExperience update(String id, WorkExperience workExperience);

    void delete(String id);
}
