package ucu.retojulio2026.talent.user;

import ucu.retojulio2026.talent.user.dto.CreateUserRequest;

import java.util.List;

public interface UserService {

    User create(CreateUserRequest request);

    // Alta de una cuenta ADMIN, ya APROBADA. No pasa por CreateUserRequest porque
    // @PublicSignupRole bloquea el rol ADMIN en el signup publico.
    User createAdmin(String email, String rawPassword);

    User getById(String id);

    List<User> getAll();

    User getByEmail(String email);

    void delete(String id);

    boolean existsById(String id);
}
