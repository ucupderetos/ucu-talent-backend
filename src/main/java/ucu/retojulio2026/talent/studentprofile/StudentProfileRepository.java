package ucu.retojulio2026.talent.studentprofile;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, String> {

    Optional<StudentProfile> findByUserId(String userId);

}
