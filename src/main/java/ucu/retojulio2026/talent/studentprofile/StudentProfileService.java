package ucu.retojulio2026.talent.studentprofile;

import ucu.retojulio2026.talent.studentprofile.dto.CreateStudentProfileRequest;
import ucu.retojulio2026.talent.studentprofile.dto.UpdateStudentProfileRequest;

import java.util.List;

public interface StudentProfileService {

    StudentProfile create(CreateStudentProfileRequest request);

    StudentProfile update(String id, UpdateStudentProfileRequest request);

    StudentProfile getById(String id);

    List<StudentProfile> getAll();

    void delete(String id);

    boolean existsById(String id);
}
