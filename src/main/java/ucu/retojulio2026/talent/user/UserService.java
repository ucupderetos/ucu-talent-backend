package ucu.retojulio2026.talent.user;

import ucu.retojulio2026.talent.user.dto.CreateUserRequest;

import java.util.List;
import java.util.Map;

public interface UserService {

    User create(CreateUserRequest request);

    User createAdmin(String email, String rawPassword);

    User getById(String id);

    List<User> getAll(AccountStatus status, Role role);

    Map<AccountStatus, Long> countByRoleGroupedByStatus(Role role);

    User getByEmail(String email);

    void delete(String id);

    boolean existsById(String id);

    void updateStatus(String userId, AccountStatus newStatus);
}
