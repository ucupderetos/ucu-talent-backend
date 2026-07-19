package ucu.retojulio2026.talent.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ucu.retojulio2026.talent.auth.dto.LoginRequest;
import ucu.retojulio2026.talent.user.User;
import ucu.retojulio2026.talent.user.UserService;
import ucu.retojulio2026.talent.common.InvalidCredentialsException;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User login(LoginRequest request) {
        User user;
        try {
            user = userService.getByEmail(request.email());
        } catch (ResourceNotFoundException e) {
            throw new InvalidCredentialsException();
        }

        boolean passwordOk = passwordEncoder.matches(request.password(), user.getPasswordHash());
        if (!passwordOk) {
            throw new InvalidCredentialsException();
        }
        return user;
    }
}
