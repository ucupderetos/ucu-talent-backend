package ucu.retojulio2026.talent.degree;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DegreeRepository extends JpaRepository<Degree, String> {

    // save, findById, deleteById, existsById, findAll, etc ya vienen de JpaRepository.
    //Busca todas las carreras que pertenecen a una misma area.
    List<Degree> findByAreaId(String areaId);
}