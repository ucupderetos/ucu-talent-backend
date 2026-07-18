package ucu.retojulio2026.talent.studentprofile;

import ucu.retojulio2026.talent.studentprofile.dto.CreateStudentProfileRequest;

public interface StudentProfileService {

    StudentProfile create(CreateStudentProfileRequest request);

    StudentProfile getById(String id);

    StudentProfile getByUserId(String userId);

    void delete(String id);

    boolean existsById(String id);
}
