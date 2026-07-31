package ucu.retojulio2026.talent.degree;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DegreeRepository extends JpaRepository<Degree, String> {

    List<Degree> findByAreaId(String areaId);
}
