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

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentProfileServiceImplTest {

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private StudentProfileMapper studentProfileMapper;

    @Mock
    private UserService userService;

    private StudentProfileServiceImpl studentProfileService;

    @BeforeEach
    void setUp() {
        studentProfileService = new StudentProfileServiceImpl(
                studentProfileRepository,
                studentProfileMapper,
                userService
        );
    }

    @Test
    void createShouldThrowResourceNotFoundExceptionWhenUserDoesNotExist() {
        String userId = "user-1";

        when(userService.existsById(userId)).thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> studentProfileService.create(userId, null)
        );

        verify(studentProfileRepository, never()).save(any());
    }

    @Test
    void createShouldThrowDuplicateResourceExceptionWhenProfileAlreadyExists() {
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
    void createShouldThrowDuplicateResourceExceptionWhenNormalizedDocumentAlreadyExists() {
        String userId = "user-1";

        CreateStudentProfileRequest request = new CreateStudentProfileRequest(
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
        when(studentProfileRepository.existsByDocumentTypeAndDocumentNumber(
                DocumentType.CEDULA_IDENTIDAD,
                "12345678"
        )).thenReturn(true);

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
    void createShouldSaveProfileWithNormalizedDocumentAndSkills() {
        String userId = "user-1";

        CreateStudentProfileRequest request = new CreateStudentProfileRequest(
                "Facundo",
                "Rodriguez",
                DocumentType.CEDULA_IDENTIDAD,
                "1.234.567-8",
                "099123456",
                "https://linkedin.com/in/facundo",
                List.of(" Java ", "SPRING BOOT", "java", " ", "SQL"),
                "Estudiante"
        );

        StudentProfile studentProfile = new StudentProfile();
        studentProfile.setSkills(request.skills());

        when(userService.existsById(userId)).thenReturn(true);
        when(studentProfileRepository.existsById(userId)).thenReturn(false);
        when(studentProfileRepository.existsByDocumentTypeAndDocumentNumber(
                DocumentType.CEDULA_IDENTIDAD,
                "12345678"
        )).thenReturn(false);
        when(studentProfileMapper.toEntity(userId, request))
                .thenReturn(studentProfile);
        when(studentProfileRepository.save(any(StudentProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        studentProfileService.create(userId, request);

        ArgumentCaptor<StudentProfile> captor =
                ArgumentCaptor.forClass(StudentProfile.class);

        verify(studentProfileRepository).save(captor.capture());

        StudentProfile savedProfile = captor.getValue();

        assertEquals("12345678", savedProfile.getDocumentNumber());
        assertEquals(
                List.of("java", "spring boot", "sql"),
                savedProfile.getSkills()
        );
    }

    @Test
    void updateShouldUpdateAllowedFieldsAndNormalizeSkills() {
        String userId = "user-1";

        StudentProfile existingProfile = new StudentProfile();
        existingProfile.setPhoneNumber("099000000");
        existingProfile.setLinkedinUrl("https://linkedin.com/in/anterior");
        existingProfile.setSkills(List.of("python"));
        existingProfile.setDescription("Descripción anterior");

        UpdateStudentProfileRequest request = new UpdateStudentProfileRequest(
                "098123456",
                "https://linkedin.com/in/facundo",
                List.of(" Java ", "SPRING BOOT", "java", " ", "SQL"),
                "Nueva descripción"
        );

        when(studentProfileRepository.findById(userId))
                .thenReturn(Optional.of(existingProfile));

        when(studentProfileRepository.save(any(StudentProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StudentProfile result =
                studentProfileService.update(userId, request);

        assertEquals("098123456", result.getPhoneNumber());
        assertEquals(
                "https://linkedin.com/in/facundo",
                result.getLinkedinUrl()
        );
        assertEquals(
                List.of("java", "spring boot", "sql"),
                result.getSkills()
        );
        assertEquals("Nueva descripción", result.getDescription());

        verify(studentProfileRepository).save(existingProfile);
    }

    @Test
    void updateShouldThrowResourceNotFoundExceptionWhenProfileDoesNotExist() {
        String userId = "user-1";

        UpdateStudentProfileRequest request = new UpdateStudentProfileRequest(
                "098123456",
                "https://linkedin.com/in/facundo",
                List.of("Java"),
                "Descripción"
        );

        when(studentProfileRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> studentProfileService.update(userId, request)
        );

        verify(studentProfileRepository, never()).save(any());
    }

    @Test
    void getAllShouldReturnAllProfilesWhenStatusIsNull() {
        StudentProfile profile1 = new StudentProfile();
        StudentProfile profile2 = new StudentProfile();

        when(studentProfileRepository.findAll())
                .thenReturn(List.of(profile1, profile2));

        List<StudentProfile> result =
                studentProfileService.getAll(null);

        assertEquals(2, result.size());

        verify(studentProfileRepository).findAll();
        verify(userService, never()).getAll(any(), any(), any());
    }

    @Test
    void getAllShouldReturnProfilesFilteredByStatus() {
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

        when(userService.getAll(
                AccountStatus.APROBADO,
                Role.ALUMNO,
                Pageable.unpaged()
        )).thenReturn(usersPage);

        when(studentProfileRepository.findAllById(
                List.of("user-1", "user-2")
        )).thenReturn(List.of(profile1, profile2));

        List<StudentProfile> result =
                studentProfileService.getAll(AccountStatus.APROBADO);

        assertEquals(2, result.size());

        verify(userService).getAll(
                AccountStatus.APROBADO,
                Role.ALUMNO,
                Pageable.unpaged()
        );

        verify(studentProfileRepository)
                .findAllById(List.of("user-1", "user-2"));

        verify(studentProfileRepository, never()).findAll();
    }

    @Test
    void reviewShouldUpdateReviewedAtAndAdminComment() {
        String userId = "user-1";
        LocalDateTime reviewedAt = LocalDateTime.of(
                2026, 7, 27, 11, 0
        );

        StudentProfile existingProfile = new StudentProfile();

        when(studentProfileRepository.findById(userId))
                .thenReturn(Optional.of(existingProfile));

        studentProfileService.review(
                userId,
                reviewedAt,
                "Perfil aprobado"
        );

        assertEquals(reviewedAt, existingProfile.getReviewedAt());
        assertEquals(
                "Perfil aprobado",
                existingProfile.getAdminComment()
        );

        verify(studentProfileRepository).save(existingProfile);
    }

    @Test
    void getByIdShouldThrowResourceNotFoundExceptionWhenProfileDoesNotExist() {
        String userId = "user-1";

        when(studentProfileRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> studentProfileService.getById(userId)
        );

        verify(studentProfileRepository, never()).save(any());
    }
}