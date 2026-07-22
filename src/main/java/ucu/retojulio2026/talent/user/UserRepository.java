package ucu.retojulio2026.talent.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {


    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByStatus(AccountStatus status);

    List<User> findByRole(Role role);

    List<User> findByStatusAndRole(AccountStatus status, Role role);

    //Optional es necesario porque hace explicito que la busqueda puede no tener resultado.
    //Asi el servico esta obligado a manejar el caso vacio, en vez de recibir
    //un null que despues tira NullPointerException.

}
