package ucu.retojulio2026.talent.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ucu.retojulio2026.talent.common.InvalidStatusTransitionException;
import ucu.retojulio2026.talent.user.dto.CreateUserRequest;
import ucu.retojulio2026.talent.user.dto.UserMapper;
import ucu.retojulio2026.talent.common.DuplicateResourceException;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

//Implementacion concreta del contrato UserService. Es el bean que Spring inyecta.
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    public User create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Ya existe un usuario con el email '" + request.email() + "'");
        }
        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        if (request.role() == Role.ALUMNO) {
            user.setStatus(AccountStatus.APROBADO);
        } else {
            user.setStatus(AccountStatus.PENDIENTE);
        }
        return userRepository.save(user);
    }

    @Override
    public User createAdmin(String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Ya existe un usuario con el email '" + email + "'");
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(Role.ADMIN);
        user.setStatus(AccountStatus.APROBADO);
        return userRepository.save(user);
    }

    @Override
    public User getById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User con id '" + id + "' no encontrado"));
    }

    @Override
    public Page<User> getAll(AccountStatus status, Role role, Pageable pageable) {
        if (status != null && role != null) {
            return userRepository.findByStatusAndRole(status, role, pageable);
        }
        if (status != null) {
            return userRepository.findByStatus(status, pageable);
        }
        if (role != null) {
            return userRepository.findByRole(role, pageable);
        }
        return userRepository.findAll(pageable);
    }

    @Override
    public Map<AccountStatus, Long> countByRoleGroupedByStatus(Role role) {
        Map<AccountStatus, Long> counts = new EnumMap<>(AccountStatus.class);
        for (AccountStatus status : AccountStatus.values()) {
            counts.put(status, userRepository.countByRoleAndStatus(role, status));
        }
        return counts;
    }

    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User con email '" + email + "' no encontrado"));
    }

    @Override
    public void delete(String id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User con id '" + id + "' no encontrado");
        }
        userRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return userRepository.existsById(id);
    }

    @Override
    public void updateStatus(String userId, AccountStatus newStaus){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User con id '" + userId + "' no encontrado"));
        if (user.getStatus() != AccountStatus.PENDIENTE) {
            throw new InvalidStatusTransitionException(
                    "Solo se puede aprobar o rechazar una cuenta pendiente");
        }
        if (newStaus == AccountStatus.PENDIENTE) {
            throw new InvalidStatusTransitionException(
                    "El nuevo estado debe ser APROBADO o RECHAZADO");
        }
        user.setStatus(newStaus);
        userRepository.save(user);
    }
}
