package ucu.retojulio2026.talent.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ucu.retojulio2026.talent.common.InvalidStatusTransitionException;
import ucu.retojulio2026.talent.storage.StorageService;
import ucu.retojulio2026.talent.storage.dto.StorageUploadResponse;
import ucu.retojulio2026.talent.user.dto.CreateUserRequest;
import ucu.retojulio2026.talent.user.dto.UserMapper;
import ucu.retojulio2026.talent.common.DuplicateResourceException;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;

import java.util.EnumMap;
import java.util.Map;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

//Implementacion concreta del contrato UserService. Es el bean que Spring inyecta.
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final StorageService storageService;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper, StorageService storageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.storageService = storageService;
    }

    private static final Set<String> ALLOWED_PROFILE_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png"
    );

    private void validateProfileImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La imagen de perfil es obligatoria."
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_PROFILE_IMAGE_TYPES.contains(contentType)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Solo se permiten imágenes JPG o PNG para la foto de perfil."
            );
        }
    }

    @Override
    public User create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Ya existe un usuario con el email '" + request.email() + "'");
        }
        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        if (request.role() == Role.ALUMNO) {
            user.setStatus(AccountStatus.PENDIENTE);
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
    public String getProfileImage(String profileObject, Jwt jwt) {
        if(!(userRepository.existsByProfileImage((profileObject)))) {
            throw new ResourceNotFoundException("User con la imagen de perfil '" + profileObject + "' no encontrado");
        }

        Instant expiresAt = jwt.getExpiresAt();

        Duration remaining = Duration.between(Instant.now(), expiresAt);

        if (remaining.isNegative() || remaining.isZero()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "El token ya expiró."
            );
        }

        return storageService
                .getSignedUrl(profileObject, remaining)
                .toString();
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
    public void updateStatus(String userId, AccountStatus newStatus){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User con id '" + userId + "' no encontrado"));
        if (newStatus == AccountStatus.PENDIENTE) {
            throw new InvalidStatusTransitionException(
                    "El nuevo estado debe ser APROBADO o RECHAZADO");
        }
        user.setStatus(newStatus);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateProfileImage(String userId, MultipartFile file) {
        validateProfileImage(file);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User con id '" + userId + "' no encontrado"));

        String oldObjectName = user.getProfileImage();

        StorageUploadResponse uploaded = storageService.upload(file, "users/profile-images");

        user.setProfileImage(uploaded.objectName());
        User saved = userRepository.save(user);

        if (oldObjectName != null && !oldObjectName.isBlank()) {
            storageService.delete(oldObjectName);
        }
        return saved;
    }

    @Override
    @Transactional
    public void deleteProfileImage(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User con id '" + userId + "' no encontrado"));

        String oldObjectName = user.getProfileImage();
        if (oldObjectName == null || oldObjectName.isBlank()) {
            throw new ResourceNotFoundException("El usuario no tiene imagen de perfil");
        }

        user.setProfileImage(null);
        userRepository.save(user);
        storageService.delete(oldObjectName);
    }

    @Override
    public long count() {
        return userRepository.count();
    }

    @Override
    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }
}
