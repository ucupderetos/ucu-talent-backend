package ucu.retojulio2026.talent.studentprofile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ucu.retojulio2026.talent.common.DocumentType;
import ucu.retojulio2026.talent.common.DuplicateResourceException;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.studentprofile.dto.CreateStudentProfileRequest;
import ucu.retojulio2026.talent.studentprofile.dto.StudentProfileMapper;
import ucu.retojulio2026.talent.studentprofile.dto.UpdateStudentProfileRequest;
import ucu.retojulio2026.talent.user.AccountStatus;
import ucu.retojulio2026.talent.user.Role;
import ucu.retojulio2026.talent.user.User;
import ucu.retojulio2026.talent.user.UserService;
import ucu.retojulio2026.talent.storage.StorageService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentProfileServiceImplTest {

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private StudentProfileMapper studentProfileMapper;

    @Mock
    private UserService userService;

    @Mock
    private StorageService storageService;

    private StudentProfileServiceImpl studentProfileService;

    @BeforeEach
    void setUp() {
        studentProfileService = new StudentProfileServiceImpl(
                studentProfileRepository,
                studentProfileMapper,
                userService,
                storageService
        );
    }

    @Test
    void crear_falla_si_el_usuario_no_existe() {
        String userId = "user-1";

        when(userService.existsById(userId)).thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> studentProfileService.create(userId, null)
        );

        verify(studentProfileRepository, never()).save(any());
    }

    @Test
    void crear_falla_si_el_perfil_ya_existe() {
        String userId = "user-1";

        when(userService.existsById(userId)).thenReturn(true);
        when(studentProfileRepository.existsById(userId)).thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> studentProfileService.create(userId, null)
        );

        verify(studentProfileRepository, never()).save(any());
    }

    @Test
    void crear_falla_si_el_documento_normalizado_ya_existe() {
        String userId = "user-1";

        CreateStudentProfileRequest request =
                new CreateStudentProfileRequest(
                        "Facundo",
                        "Rodriguez",
                        DocumentType.CEDULA_IDENTIDAD,
                        "1.234.567-8",
                        "099123456",
                        "https://linkedin.com/in/facundo",
                        List.of("Java"),
                        "Estudiante"
                );

        when(userService.existsById(userId)).thenReturn(true);
        when(studentProfileRepository.existsById(userId)).thenReturn(false);
        when(studentProfileRepository
                .existsByDocumentTypeAndDocumentNumber(
                        DocumentType.CEDULA_IDENTIDAD,
                        "12345678"
                ))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> studentProfileService.create(userId, request)
        );

        verify(studentProfileRepository)
                .existsByDocumentTypeAndDocumentNumber(
                        DocumentType.CEDULA_IDENTIDAD,
                        "12345678"
                );

        verify(studentProfileRepository, never()).save(any());
    }

    @Test
    void crear_guarda_el_documento_normalizado() {
        String userId = "user-1";

        CreateStudentProfileRequest request =
                new CreateStudentProfileRequest(
                        "Facundo",
                        "Rodriguez",
                        DocumentType.CEDULA_IDENTIDAD,
                        "1.234.567-8",
                        "099123456",
                        "https://linkedin.com/in/facundo",
                        List.of("Java"),
                        "Estudiante"
                );

        StudentProfile studentProfile = new StudentProfile();
        studentProfile.setSkills(request.skills());

        when(userService.existsById(userId)).thenReturn(true);
        when(studentProfileRepository.existsById(userId)).thenReturn(false);
        when(studentProfileRepository
                .existsByDocumentTypeAndDocumentNumber(
                        DocumentType.CEDULA_IDENTIDAD,
                        "12345678"
                ))
                .thenReturn(false);
        when(studentProfileMapper.toEntity(userId, request))
                .thenReturn(studentProfile);
        when(studentProfileRepository.save(any(StudentProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        studentProfileService.create(userId, request);

        ArgumentCaptor<StudentProfile> captor =
                ArgumentCaptor.forClass(StudentProfile.class);

        verify(studentProfileRepository).save(captor.capture());

        assertEquals(
                "12345678",
                captor.getValue().getDocumentNumber()
        );
    }

    @Test
    void crear_normaliza_las_skills() {
        String userId = "user-1";

        CreateStudentProfileRequest request =
                new CreateStudentProfileRequest(
                        "Facundo",
                        "Rodriguez",
                        DocumentType.CEDULA_IDENTIDAD,
                        "1.234.567-8",
                        "099123456",
                        "https://linkedin.com/in/facundo",
                        List.of(
                                " Java ",
                                "SPRING BOOT",
                                "java",
                                " ",
                                "",
                                "SQL"
                        ),
                        "Estudiante"
                );

        StudentProfile studentProfile = new StudentProfile();
        studentProfile.setSkills(request.skills());

        when(userService.existsById(userId)).thenReturn(true);
        when(studentProfileRepository.existsById(userId)).thenReturn(false);
        when(studentProfileRepository
                .existsByDocumentTypeAndDocumentNumber(
                        DocumentType.CEDULA_IDENTIDAD,
                        "12345678"
                ))
                .thenReturn(false);
        when(studentProfileMapper.toEntity(userId, request))
                .thenReturn(studentProfile);
        when(studentProfileRepository.save(any(StudentProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        studentProfileService.create(userId, request);

        ArgumentCaptor<StudentProfile> captor =
                ArgumentCaptor.forClass(StudentProfile.class);

        verify(studentProfileRepository).save(captor.capture());

        assertEquals(
                List.of("java", "spring boot", "sql"),
                captor.getValue().getSkills()
        );
    }

    @Test
    void crear_con_skills_null_guarda_lista_vacia() {
        String userId = "user-1";

        CreateStudentProfileRequest request =
                new CreateStudentProfileRequest(
                        "Facundo",
                        "Rodriguez",
                        DocumentType.CEDULA_IDENTIDAD,
                        "1.234.567-8",
                        "099123456",
                        "https://linkedin.com/in/facundo",
                        null,
                        "Estudiante"
                );

        StudentProfile studentProfile = new StudentProfile();
        studentProfile.setSkills(null);

        when(userService.existsById(userId)).thenReturn(true);
        when(studentProfileRepository.existsById(userId)).thenReturn(false);
        when(studentProfileRepository
                .existsByDocumentTypeAndDocumentNumber(
                        DocumentType.CEDULA_IDENTIDAD,
                        "12345678"
                ))
                .thenReturn(false);
        when(studentProfileMapper.toEntity(userId, request))
                .thenReturn(studentProfile);
        when(studentProfileRepository.save(any(StudentProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StudentProfile result =
                studentProfileService.create(userId, request);

        assertEquals(List.of(), result.getSkills());

        verify(studentProfileRepository).save(studentProfile);
    }

    @Test
    void actualizar_normaliza_las_skills() {
        String userId = "user-1";

        StudentProfile existingProfile = new StudentProfile();
        existingProfile.setSkills(List.of("python"));

        UpdateStudentProfileRequest request =
                new UpdateStudentProfileRequest(
                        "098123456",
                        "https://linkedin.com/in/facundo",
                        List.of(
                                " Java ",
                                "SPRING BOOT",
                                "java",
                                " ",
                                "",
                                "SQL"
                        ),
                        "Nueva descripción"
                );

        when(studentProfileRepository.findById(userId))
                .thenReturn(java.util.Optional.of(existingProfile));
        when(studentProfileRepository.save(existingProfile))
                .thenReturn(existingProfile);

        StudentProfile result =
                studentProfileService.update(userId, request);

        assertEquals(
                List.of("java", "spring boot", "sql"),
                result.getSkills()
        );

        verify(studentProfileRepository).save(existingProfile);
    }

    @Test
    void actualizar_solo_modifica_los_campos_permitidos() {
        String userId = "user-1";

        StudentProfile existingProfile = new StudentProfile();
        existingProfile.setStudentProfileId(userId);
        existingProfile.setName("Facundo");
        existingProfile.setSurname("Rodriguez");
        existingProfile.setDocumentType(
                DocumentType.CEDULA_IDENTIDAD
        );
        existingProfile.setDocumentNumber("12345678");
        existingProfile.setPhoneNumber("099000000");
        existingProfile.setLinkedinUrl(
                "https://linkedin.com/in/anterior"
        );
        existingProfile.setSkills(List.of("python"));
        existingProfile.setDescription("Descripción anterior");

        UpdateStudentProfileRequest request =
                new UpdateStudentProfileRequest(
                        "098123456",
                        "https://linkedin.com/in/facundo",
                        List.of("Java"),
                        "Nueva descripción"
                );

        when(studentProfileRepository.findById(userId))
                .thenReturn(java.util.Optional.of(existingProfile));
        when(studentProfileRepository.save(existingProfile))
                .thenReturn(existingProfile);

        StudentProfile result =
                studentProfileService.update(userId, request);

        assertEquals("098123456", result.getPhoneNumber());
        assertEquals(
                "https://linkedin.com/in/facundo",
                result.getLinkedinUrl()
        );
        assertEquals(List.of("java"), result.getSkills());
        assertEquals(
                "Nueva descripción",
                result.getDescription()
        );

        assertEquals(userId, result.getStudentProfileId());
        assertEquals("Facundo", result.getName());
        assertEquals("Rodriguez", result.getSurname());
        assertEquals(
                DocumentType.CEDULA_IDENTIDAD,
                result.getDocumentType()
        );
        assertEquals("12345678", result.getDocumentNumber());

        verify(studentProfileRepository).save(existingProfile);
    }

    @Test
    void listar_resuelve_por_estado_y_con_null_devuelve_todos() {
        StudentProfile profile1 = new StudentProfile();
        profile1.setStudentProfileId("user-1");

        StudentProfile profile2 = new StudentProfile();
        profile2.setStudentProfileId("user-2");

        User user1 = mock(User.class);
        User user2 = mock(User.class);

        when(user1.getUserId()).thenReturn("user-1");
        when(user2.getUserId()).thenReturn("user-2");

        Page<User> usersPage =
                new PageImpl<>(List.of(user1, user2));

        when(studentProfileRepository.findAll())
                .thenReturn(List.of(profile1, profile2));

        when(userService.getAll(
                AccountStatus.APROBADO,
                Role.ALUMNO,
                Pageable.unpaged()
        )).thenReturn(usersPage);

        when(studentProfileRepository.findAllById(
                List.of("user-1", "user-2")
        )).thenReturn(List.of(profile1, profile2));

        List<StudentProfile> allProfiles =
                studentProfileService.getAll(null);

        List<StudentProfile> filteredProfiles =
                studentProfileService.getAll(
                        AccountStatus.APROBADO
                );

        assertEquals(2, allProfiles.size());
        assertEquals(2, filteredProfiles.size());

        verify(studentProfileRepository).findAll();

        verify(userService).getAll(
                AccountStatus.APROBADO,
                Role.ALUMNO,
                Pageable.unpaged()
        );

        verify(studentProfileRepository)
                .findAllById(List.of("user-1", "user-2"));
    }

    @Test
    void revisar_guarda_la_fecha_y_el_comentario() {
        String userId = "user-1";

        LocalDateTime reviewedAt =
                LocalDateTime.of(2026, 7, 27, 11, 0);

        StudentProfile existingProfile =
                new StudentProfile();

        when(studentProfileRepository.findById(userId))
                .thenReturn(java.util.Optional.of(existingProfile));

        studentProfileService.review(
                userId,
                reviewedAt,
                "Perfil aprobado"
        );

        assertEquals(
                reviewedAt,
                existingProfile.getReviewedAt()
        );
        assertEquals(
                "Perfil aprobado",
                existingProfile.getAdminComment()
        );

        verify(studentProfileRepository)
                .save(existingProfile);
    }
}