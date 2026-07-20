package ucu.retojulio2026.talent.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ucu.retojulio2026.talent.user.dto.CreateUserRequest;


@Service
public class UserRegistrationServiceImpl implements UserRegistrationService {

    private final UserService userService;

    public UserRegistrationServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    @Transactional
    public User register(CreateUserRequest request) {
        return userService.create(request);
    }
}
