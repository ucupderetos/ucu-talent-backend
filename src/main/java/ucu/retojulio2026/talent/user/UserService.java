package ucu.retojulio2026.talent.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;
import ucu.retojulio2026.talent.user.dto.CreateUserRequest;

import java.util.List;
import java.util.Map;

public interface UserService {

    User create(CreateUserRequest request);

    User createAdmin(String email, String rawPassword);

    User getById(String id);

    Page<User> getAll(AccountStatus status, Role role, Pageable pageable);

    Map<AccountStatus, Long> countByRoleGroupedByStatus(Role role);

    User getByEmail(String email);

    void delete(String id);

    boolean existsById(String id);

    void updateStatus(String userId, AccountStatus newStatus);

    String getProfileImage(String profileObject, Jwt jwt);

    User updateProfileImage(String userId, MultipartFile file);

    void deleteProfileImage(String userId);

    long count();

    long countByRole(Role role);
}
