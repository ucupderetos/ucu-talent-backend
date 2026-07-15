package ucu.retojulio2026.talent.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ucu.retojulio2026.talent.auth.dto.LoginRequest;
import ucu.retojulio2026.talent.user.User;
import ucu.retojulio2026.talent.user.UserRepository;
import ucu.retojulio2026.talent.common.InvalidCredentialsException;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        boolean passwordOk = passwordEncoder.matches(request.password(), user.getPasswordHash());
        if (!passwordOk) {
            throw new InvalidCredentialsException();
        }
        return user;
    }
}
