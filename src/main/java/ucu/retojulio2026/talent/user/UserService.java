package ucu.retojulio2026.talent.user;

import ucu.retojulio2026.talent.user.dto.CreateUserRequest;
import ucu.retojulio2026.talent.user.dto.UpdateUserRequest;

public interface UserService {

    User create(CreateUserRequest request);

    User getById(String id);

    User getByEmail(String email);

    User update(String id, UpdateUserRequest request);

    void delete(String id);
}
