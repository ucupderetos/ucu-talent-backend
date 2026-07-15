package ucu.retojulio2026.talent.education;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EducationRepository extends JpaRepository<Education, String> {
    
    // listar todas las educaciones de un perfil de alumno.
    List<Education> getByPerfilAlumnoId(String perfilAlumnoId);
}
