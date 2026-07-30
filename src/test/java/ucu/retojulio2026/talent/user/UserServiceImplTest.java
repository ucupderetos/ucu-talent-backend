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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import ucu.retojulio2026.talent.common.DuplicateResourceException;
import ucu.retojulio2026.talent.common.InvalidStatusTransitionException;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.storage.StorageService;
import ucu.retojulio2026.talent.user.dto.CreateUserRequest;
import ucu.retojulio2026.talent.user.dto.UserMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

    @Mock
    private StorageService storageService;

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
        UserServiceImpl service = new UserServiceImpl(userRepository, passwordEncoder, userMapper, storageService);
        CreateUserRequest request = new CreateUserRequest("nicogon@ucu.edu.uy", RAW_PASSWORD, Role.ALUMNO);
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.create(request));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registro_hashea_la_contrasena_antes_de_guardar() {
        UserServiceImpl service = new UserServiceImpl(userRepository, passwordEncoder, userMapper, storageService);
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
        UserServiceImpl service = new UserServiceImpl(userRepository, passwordEncoder, userMapper, storageService);
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

    @Test
    void alta_de_admin_con_email_duplicado_lanza_duplicate_resource_exception() {
        UserServiceImpl service = new UserServiceImpl(userRepository, passwordEncoder, userMapper, storageService);
        when(userRepository.existsByEmail("admin@ucu.edu.uy")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> service.createAdmin("admin@ucu.edu.uy", RAW_PASSWORD));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void alta_de_admin_queda_aprobada_con_role_admin_y_status_aprobado() {
        UserServiceImpl service = new UserServiceImpl(userRepository, passwordEncoder, userMapper, storageService);
        when(userRepository.existsByEmail("admin@ucu.edu.uy")).thenReturn(false);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = service.createAdmin("admin@ucu.edu.uy", RAW_PASSWORD);

        assertThat(created.getRole()).isEqualTo(Role.ADMIN);
        assertThat(created.getStatus()).isEqualTo(AccountStatus.APROBADO);
    }

    @Test
    void no_se_puede_volver_a_pendiente_una_cuenta_ya_revisada() {
        UserServiceImpl service = new UserServiceImpl(userRepository, passwordEncoder, userMapper, storageService);
        User user = newMappedUser(Role.ALUMNO);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        assertThrows(InvalidStatusTransitionException.class,
                () -> service.updateStatus("user-1", AccountStatus.PENDIENTE));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void revisar_un_usuario_inexistente_lanza_resource_not_found_exception() {
        UserServiceImpl service = new UserServiceImpl(userRepository, passwordEncoder, userMapper, storageService);
        when(userRepository.findById("user-x")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateStatus("user-x", AccountStatus.APROBADO));
    }

    @ParameterizedTest
    @EnumSource(value = AccountStatus.class, names = {"APROBADO", "RECHAZADO"})
    void revisar_un_usuario_actualiza_y_guarda_el_nuevo_estado(AccountStatus nuevoEstado) {
        UserServiceImpl service = new UserServiceImpl(userRepository, passwordEncoder, userMapper, storageService);
        User user = newMappedUser(Role.ALUMNO);
        user.setStatus(AccountStatus.PENDIENTE);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        service.updateStatus("user-1", nuevoEstado);

        assertThat(user.getStatus()).isEqualTo(nuevoEstado);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void buscar_por_email_inexistente_lanza_resource_not_found_exception() {
        UserServiceImpl service = new UserServiceImpl(userRepository, passwordEncoder, userMapper, storageService);
        when(userRepository.findByEmail("nadie@ucu.edu.uy")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getByEmail("nadie@ucu.edu.uy"));
    }

    @Test
    void borrar_un_usuario_inexistente_lanza_resource_not_found_exception() {
        UserServiceImpl service = new UserServiceImpl(userRepository, passwordEncoder, userMapper, storageService);
        when(userRepository.existsById("user-x")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.delete("user-x"));

        verify(userRepository, never()).deleteById(anyString());
    }

    @Test
    void listar_con_status_y_role_filtra_por_ambos() {
        UserServiceImpl service = new UserServiceImpl(userRepository, passwordEncoder, userMapper, storageService);
        Pageable pageable = mock(Pageable.class);
        Page<User> page = new PageImpl<>(List.of(newMappedUser(Role.ALUMNO)));
        when(userRepository.findByStatusAndRole(AccountStatus.APROBADO, Role.ALUMNO, pageable)).thenReturn(page);

        Page<User> result = service.getAll(AccountStatus.APROBADO, Role.ALUMNO, pageable);

        assertThat(result).isEqualTo(page);
        verify(userRepository, never()).findByStatus(any(), any());
        verify(userRepository, never()).findByRole(any(), any());
        verify(userRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void listar_solo_con_status_filtra_por_status() {
        UserServiceImpl service = new UserServiceImpl(userRepository, passwordEncoder, userMapper, storageService);
        Pageable pageable = mock(Pageable.class);
        Page<User> page = new PageImpl<>(List.of(newMappedUser(Role.ALUMNO)));
        when(userRepository.findByStatus(AccountStatus.PENDIENTE, pageable)).thenReturn(page);

        Page<User> result = service.getAll(AccountStatus.PENDIENTE, null, pageable);

        assertThat(result).isEqualTo(page);
        verify(userRepository, never()).findByStatusAndRole(any(), any(), any());
        verify(userRepository, never()).findByRole(any(), any());
        verify(userRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void listar_solo_con_role_filtra_por_role() {
        UserServiceImpl service = new UserServiceImpl(userRepository, passwordEncoder, userMapper, storageService);
        Pageable pageable = mock(Pageable.class);
        Page<User> page = new PageImpl<>(List.of(newMappedUser(Role.EMPRESA)));
        when(userRepository.findByRole(Role.EMPRESA, pageable)).thenReturn(page);

        Page<User> result = service.getAll(null, Role.EMPRESA, pageable);

        assertThat(result).isEqualTo(page);
        verify(userRepository, never()).findByStatusAndRole(any(), any(), any());
        verify(userRepository, never()).findByStatus(any(), any());
        verify(userRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void listar_sin_filtros_devuelve_todos_los_usuarios() {
        UserServiceImpl service = new UserServiceImpl(userRepository, passwordEncoder, userMapper, storageService);
        Pageable pageable = mock(Pageable.class);
        Page<User> page = new PageImpl<>(List.of(newMappedUser(Role.ADMIN)));
        when(userRepository.findAll(pageable)).thenReturn(page);

        Page<User> result = service.getAll(null, null, pageable);

        assertThat(result).isEqualTo(page);
        verify(userRepository, never()).findByStatusAndRole(any(), any(), any());
        verify(userRepository, never()).findByStatus(any(), any());
        verify(userRepository, never()).findByRole(any(), any());
    }

    @Test
    void panel_de_actividad_trae_las_tres_claves_de_estado_aunque_el_conteo_sea_cero() {
        UserServiceImpl service = new UserServiceImpl(userRepository, passwordEncoder, userMapper, storageService);
        when(userRepository.countByRoleAndStatus(Role.ALUMNO, AccountStatus.PENDIENTE)).thenReturn(3L);
        when(userRepository.countByRoleAndStatus(Role.ALUMNO, AccountStatus.APROBADO)).thenReturn(5L);
        when(userRepository.countByRoleAndStatus(Role.ALUMNO, AccountStatus.RECHAZADO)).thenReturn(0L);

        Map<AccountStatus, Long> counts = service.countByRoleGroupedByStatus(Role.ALUMNO);

        assertThat(counts)
                .containsOnlyKeys(AccountStatus.PENDIENTE, AccountStatus.APROBADO, AccountStatus.RECHAZADO)
                .containsEntry(AccountStatus.PENDIENTE, 3L)
                .containsEntry(AccountStatus.APROBADO, 5L)
                .containsEntry(AccountStatus.RECHAZADO, 0L);
    }
}
