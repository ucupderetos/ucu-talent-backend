package ucu.retojulio2026.talent.studentprofile;

import ucu.retojulio2026.talent.studentprofile.dto.CreateStudentProfileRequest;

import java.util.List;

public interface StudentProfileService {

    StudentProfile create(CreateStudentProfileRequest request);

    StudentProfile getById(String id);

    List<StudentProfile> getAll();

    StudentProfile getByUserId(String userId);

    void delete(String id);

    boolean existsById(String id);
}
