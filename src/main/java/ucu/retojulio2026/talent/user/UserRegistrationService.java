package ucu.retojulio2026.talent.user;

import ucu.retojulio2026.talent.user.dto.CreateUserRequest;

public interface UserRegistrationService {


    User register(CreateUserRequest request);
}
