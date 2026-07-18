package ucu.retojulio2026.talent.user;

import ucu.retojulio2026.talent.user.dto.CreateUserRequest;
import ucu.retojulio2026.talent.user.dto.UpdateUserRequest;

import java.util.List;

public interface UserService {

    User create(CreateUserRequest request);

    User getById(String id);

    List<User> getAll();

    User getByEmail(String email);

    User update(String id, UpdateUserRequest request);

    void delete(String id);

    boolean existsById(String id);
}
