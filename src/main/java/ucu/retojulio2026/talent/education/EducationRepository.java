package ucu.retojulio2026.talent.education;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EducationRepository extends JpaRepository<Education, String> {
  
    // List all education entries for a student profile.
    List<Education> findByStudentProfileId(String studentProfileId);
}
