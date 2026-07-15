package ucu.retojulio2026.talent.user;

import ucu.retojulio2026.talent.user.dto.CreateUserRequest;

public interface UserService {

    User create(CreateUserRequest request);

    User getById(String id);

    User getByEmail(String email);

    void delete(String id);
}
