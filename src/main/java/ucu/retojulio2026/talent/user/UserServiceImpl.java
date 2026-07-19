package ucu.retojulio2026.talent.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ucu.retojulio2026.talent.user.dto.CreateUserRequest;
import ucu.retojulio2026.talent.user.dto.UpdateUserRequest;
import ucu.retojulio2026.talent.user.dto.UserMapper;
import ucu.retojulio2026.talent.common.DuplicateResourceException;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;

import java.util.List;

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
        // Chequeo previo para el caso comun (falla rapido, mensaje claro). No reemplaza la
        // constraint uq_user_email de la base (V1__create_user_table.sql): ante una carrera
        // real (dos signups simultaneos con el mismo email), la base sigue siendo la unica
        // garantia real de unicidad -> DataIntegrityViolationException, atrapada en
        // GlobalExceptionHandler igual que este caso.
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Ya existe un usuario con el email '" + request.email() + "'");
        }
        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        return userRepository.save(user);
    }

    @Override
    public User getById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User con id '" + id + "' no encontrado"));
        // Si no hay usuario, lanza la excepcion. Spring la rutea al GlobalExceptionHandler
        // clase global con @RestControllerAdvice, que la traduce a un 404 en el metodo handleNotFound.
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User con email '" + email + "' no encontrado"));
    }

    @Override
    public User update(String id, UpdateUserRequest request) {
        User user = getById(id); // lanza 404 si no existe
        user.setName(request.name());
        user.setSurname(request.surname());
        user.setPhoneNumber(request.phoneNumber());
        user.setDocumentType(request.documentType());
        user.setDocumentNumber(request.documentNumber());
        user.setLinkedinUrl(request.linkedinUrl());
        return userRepository.save(user);
        // No se tocan email, passwordHash ni role: se cambian por flujos aparte.
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
}
