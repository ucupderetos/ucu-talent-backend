package ucu.retojulio2026.talent.auth;

import ucu.retojulio2026.talent.auth.dto.LoginRequest;
import ucu.retojulio2026.talent.user.User;

public interface AuthService {

    User login(LoginRequest request);
}
