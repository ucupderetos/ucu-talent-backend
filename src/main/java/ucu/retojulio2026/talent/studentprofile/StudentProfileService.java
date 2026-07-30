package ucu.retojulio2026.talent.studentprofile;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;
import ucu.retojulio2026.talent.studentprofile.dto.CreateStudentProfileRequest;
import ucu.retojulio2026.talent.studentprofile.dto.UpdateStudentProfileRequest;
import ucu.retojulio2026.talent.user.AccountStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface StudentProfileService {

    StudentProfile create(String id, CreateStudentProfileRequest request);

    StudentProfile update(String id, UpdateStudentProfileRequest request);

    StudentProfile getById(String id);

    List<StudentProfile> getAll(AccountStatus status);

    void delete(String id);

    boolean existsById(String id);

    Map<AccountStatus, Long> getStatusSummary();

    void review(String id, LocalDateTime reviewedAt, String adminComment);

    boolean hasProfile(String id);

    StudentProfile updateCvFile(String id, MultipartFile file);

    void deleteCvFile(String id);

    String getCvFile(String cvFile, Jwt jwt);
}
