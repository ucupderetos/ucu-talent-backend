package ucu.retojulio2026.talent.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // save, findById, deleteById, existsById, findAll, etc ya vienen de JpaRepository.
    // Solo hacer  queries propias que no vienen por defecto, ejemplo:
    Optional<User> findByEmail(String email);

    //Optional es necesario porque hace explicito que la busqueda puede no tener resultado.
    //Asi el servico esta obligado a manejar el caso vacio, en vez de recibir
    //un null que despues tira NullPointerException.

}
