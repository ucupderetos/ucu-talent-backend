package ucu.retojulio2026.talent.workexperience;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkExperienceRepository extends JpaRepository<WorkExperience, String> {

    List<WorkExperience> findByPerfilAlumnoId(String perfilAlumnoId);
}
