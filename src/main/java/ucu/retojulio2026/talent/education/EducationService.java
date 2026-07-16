package ucu.retojulio2026.talent.education;

import java.util.List;

public interface EducationService {

    Education create(Education education);

    Education getByEducationId(String education_id);

    List<Education> getByStudentProfileId(String studentProfileId);

    Education update(String education_id, Education education);

    void delete(String education_id);
}
