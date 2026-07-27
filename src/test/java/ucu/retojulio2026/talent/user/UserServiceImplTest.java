package ucu.retojulio2026.talent.user;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ucu.retojulio2026.talent.common.DuplicateResourceException;
import ucu.retojulio2026.talent.user.dto.CreateUserRequest;
import ucu.retojulio2026.talent.user.dto.UserMapper;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final String RAW_PASSWORD = "unaClaveSegura123";
    private static final String HASHED_PASSWORD = "$2a$10$hashSimuladoDeBcrypt.................";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    private User newMappedUser(Role role) {
        User user = new User();
        user.setEmail("nicogon@ucu.edu.uy");
        user.setRole(role);
        return user;
    }

    @Test
    void registro_falla_si_el_email_ya_existe() {
        UserServiceImpl service = new UserServiceImpl(userRepository, passwordEncoder, userMapper);
        CreateUserRequest request = new CreateUserRequest("nicogon@ucu.edu.uy", RAW_PASSWORD, Role.ALUMNO);
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.create(request));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registro_hashea_la_contrasena_antes_de_guardar() {
        UserServiceImpl service = new UserServiceImpl(userRepository, passwordEncoder, userMapper);
        CreateUserRequest request = new CreateUserRequest("nicogon@ucu.edu.uy", RAW_PASSWORD, Role.ALUMNO);
        User mapped = newMappedUser(Role.ALUMNO);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(mapped);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = service.create(request);

        assertThat(created.getPasswordHash()).isEqualTo(HASHED_PASSWORD);
        assertThat(created.getPasswordHash()).isNotEqualTo(RAW_PASSWORD);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ALUMNO", "EMPRESA"})
    void registro_deja_al_usuario_en_estado_pendiente_de_aprobacion_del_admin(Role role) {
        UserServiceImpl service = new UserServiceImpl(userRepository, passwordEncoder, userMapper);
        CreateUserRequest request = new CreateUserRequest("nicogon@ucu.edu.uy", RAW_PASSWORD, role);
        User mapped = newMappedUser(role);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(mapped);
        when(passwordEncoder.encode(anyString())).thenReturn(HASHED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = service.create(request);

        assertThat(created.getStatus()).isEqualTo(AccountStatus.PENDIENTE);
    }

    @Test
    void contrasena_de_menos_de_8_caracteres_es_invalida_segun_rn12() {
        CreateUserRequest request = new CreateUserRequest("nicogon@ucu.edu.uy", "corta1", Role.ALUMNO);

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }
}
