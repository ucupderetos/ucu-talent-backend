package ucu.retojulio2026.talent.degree;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DegreeRepository extends JpaRepository<Degree, String> {

    // save, findById, deleteById, existsById, findAll, etc ya vienen de JpaRepository.
    // Solo hacer queries propias que no vienen por defecto, ejemplo:
    Optional<Degree> findByNameIgnoreCase(String name);

    //Busca todas las carreras que pertenecen a una misma area.
    List<Degree> findByAreaId(String areaId);

    //Optional es necesario porque hace explicito que la busqueda puede no tener resultado.
    //Asi el servicio esta obligado a manejar el caso vacio, en vez de recibir
    //un null que despues tira NullPointerException.
}