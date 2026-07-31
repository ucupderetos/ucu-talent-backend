package ucu.retojulio2026.talent.user;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByStatus(AccountStatus status, Pageable pageable);

    Page<User> findByRole(Role role, Pageable pageable);

    Page<User> findByStatusAndRole(AccountStatus status, Role role, Pageable pageable);

    long countByRoleAndStatus(Role role, AccountStatus status);

    long countByRole(Role role);

    boolean existsByProfileImage(String profileImage);

}
