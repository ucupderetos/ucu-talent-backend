package ucu.retojulio2026.talent.studentprofile;

import ucu.retojulio2026.talent.studentprofile.dto.CreateStudentProfileRequest;
import ucu.retojulio2026.talent.studentprofile.dto.UpdateStudentProfileRequest;
import ucu.retojulio2026.talent.user.AccountStatus;

import java.util.List;
import java.util.Map;

public interface StudentProfileService {

    StudentProfile create(CreateStudentProfileRequest request);

    StudentProfile update(String id, UpdateStudentProfileRequest request);

    StudentProfile getById(String id);

    List<StudentProfile> getAll();

    void delete(String id);

    boolean existsById(String id);

    Map<AccountStatus, Long> getStatusSummary();
}
