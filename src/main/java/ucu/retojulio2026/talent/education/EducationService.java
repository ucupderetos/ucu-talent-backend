package ucu.retojulio2026.talent.education;

import ucu.retojulio2026.talent.education.dto.CreateEducationRequest;
import ucu.retojulio2026.talent.education.dto.UpdateEducationRequest;

import java.util.List;

public interface EducationService {

    Education create(CreateEducationRequest request);

    Education getByEducationId(String educationId);

    List<Education> getByStudentProfileId(String studentProfileId);

    Education update(String educationId, UpdateEducationRequest request);

    void delete(String educationId);
}
