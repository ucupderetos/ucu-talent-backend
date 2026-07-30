package ucu.retojulio2026.talent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import ucu.retojulio2026.talent.common.AccountNotApprovedException;
import ucu.retojulio2026.talent.common.DuplicateResourceException;
import ucu.retojulio2026.talent.common.InvalidStatusTransitionException;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.education.Education;
import ucu.retojulio2026.talent.education.EducationService;
import ucu.retojulio2026.talent.studentprofile.StudentProfileService;
import ucu.retojulio2026.talent.user.AccountStatus;
import ucu.retojulio2026.talent.user.User;
import ucu.retojulio2026.talent.user.UserService;
import ucu.retojulio2026.talent.vacancy.Vacancy;
import ucu.retojulio2026.talent.vacancy.VacancyService;
import ucu.retojulio2026.talent.vacancy.VacancyStatus;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplication;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationCreatedEvent;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationRepository;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationServiceImpl;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationStatus;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationStatusChangedEvent;
import ucu.retojulio2026.talent.vacancyapplication.dto.CreateVacancyApplicationRequest;
import ucu.retojulio2026.talent.vacancyapplication.dto.VacancyApplicationMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VacancyApplicationServiceImplTest {

    @Mock private VacancyApplicationRepository vacancyApplicationRepository;
    @Mock private StudentProfileService studentProfileService;
    @Mock private EducationService educationService;
    @Mock private VacancyApplicationMapper vacancyApplicationMapper;
    @Mock private VacancyService vacancyService;
    @Mock private UserService userService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private VacancyApplicationServiceImpl service;

    @Test
    void create_caminoFeliz_guardaConStatusPendienteYAppliedAt() {
        String vacancyId = "vac123456789";
        String studentId = "stu123456789";
        String companyId = "cmp123456789";

        CreateVacancyApplicationRequest request =
                new CreateVacancyApplicationRequest(vacancyId, studentId, null, null);

        Vacancy vacancy = new Vacancy();
        vacancy.setVacancyId(vacancyId);
        vacancy.setCompanyId(companyId);
        vacancy.setName("Backend Developer");
        vacancy.setStatus(VacancyStatus.PUBLICADO);

        User studentUser = new User();
        studentUser.setStatus(AccountStatus.APROBADO);

        VacancyApplication entity = new VacancyApplication();
        entity.setVacancyId(vacancyId);
        entity.setStudentProfileId(studentId);

        when(vacancyService.getVacancyById(vacancyId)).thenReturn(vacancy);
        when(studentProfileService.existsById(studentId)).thenReturn(true);
        when(userService.getById(studentId)).thenReturn(studentUser);
        when(educationService.getByStudentProfileId(studentId)).thenReturn(List.of(new Education()));
        when(vacancyApplicationRepository.existsByVacancyIdAndStudentProfileId(vacancyId, studentId)).thenReturn(false);
        when(vacancyApplicationMapper.toEntity(request)).thenReturn(entity);
        when(vacancyApplicationRepository.save(any(VacancyApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VacancyApplication result = service.create(request);

        assertEquals(VacancyApplicationStatus.PENDIENTE, result.getStatus());
        assertNotNull(result.getAppliedAt());
        assertEquals(LocalDate.now(), result.getAppliedAt());

        verify(vacancyApplicationRepository).save(entity);

        ArgumentCaptor<VacancyApplicationCreatedEvent> event =
                ArgumentCaptor.forClass(VacancyApplicationCreatedEvent.class);
        verify(eventPublisher).publishEvent(event.capture());
        assertEquals(vacancyId, event.getValue().vacancyId());
        assertEquals(studentId, event.getValue().studentProfileId());
    }

    @Test
    void create_vacanteNoPublicada_lanza409() {
        String vacancyId = "vac123456789";
        String studentId = "stu123456789";
        CreateVacancyApplicationRequest request =
                new CreateVacancyApplicationRequest(vacancyId, studentId, null, null);

        Vacancy vacancy = new Vacancy();
        vacancy.setStatus(VacancyStatus.PENDIENTE);

        when(vacancyService.getVacancyById(vacancyId)).thenReturn(vacancy);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.create(request));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(vacancyApplicationRepository, never()).save(any());
    }

    @Test
    void create_alumnoInexistente_lanzaResourceNotFound() {
        String vacancyId = "vac123456789";
        String studentId = "stu123456789";
        CreateVacancyApplicationRequest request =
                new CreateVacancyApplicationRequest(vacancyId, studentId, null, null);

        Vacancy vacancy = new Vacancy();
        vacancy.setStatus(VacancyStatus.PUBLICADO);

        when(vacancyService.getVacancyById(vacancyId)).thenReturn(vacancy);
        when(studentProfileService.existsById(studentId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.create(request));
        verify(vacancyApplicationRepository, never()).save(any());
    }

    @Test
    void create_alumnoNoAprobado_lanzaAccountNotApproved() {
        String vacancyId = "vac123456789";
        String studentId = "stu123456789";
        CreateVacancyApplicationRequest request =
                new CreateVacancyApplicationRequest(vacancyId, studentId, null, null);

        Vacancy vacancy = new Vacancy();
        vacancy.setStatus(VacancyStatus.PUBLICADO);

        User studentUser = new User();
        studentUser.setStatus(AccountStatus.PENDIENTE);

        when(vacancyService.getVacancyById(vacancyId)).thenReturn(vacancy);
        when(studentProfileService.existsById(studentId)).thenReturn(true);
        when(userService.getById(studentId)).thenReturn(studentUser);

        assertThrows(AccountNotApprovedException.class, () -> service.create(request));
        verify(vacancyApplicationRepository, never()).save(any());
    }

    @Test
    void create_alumnoRechazado_lanzaAccountNotApprovedConMensajeDeRechazo() {
        String vacancyId = "vac123456789";
        String studentId = "stu123456789";
        CreateVacancyApplicationRequest request =
                new CreateVacancyApplicationRequest(vacancyId, studentId, null, null);

        Vacancy vacancy = new Vacancy();
        vacancy.setStatus(VacancyStatus.PUBLICADO);

        User studentUser = new User();
        studentUser.setStatus(AccountStatus.RECHAZADO);

        when(vacancyService.getVacancyById(vacancyId)).thenReturn(vacancy);
        when(studentProfileService.existsById(studentId)).thenReturn(true);
        when(userService.getById(studentId)).thenReturn(studentUser);

        AccountNotApprovedException ex = assertThrows(AccountNotApprovedException.class,
                () -> service.create(request));
        assertTrue(ex.getMessage().contains("RECHAZADO"));
        verify(vacancyApplicationRepository, never()).save(any());
    }

    @Test
    void create_sinEducacion_lanza409() {
        String vacancyId = "vac123456789";
        String studentId = "stu123456789";
        CreateVacancyApplicationRequest request =
                new CreateVacancyApplicationRequest(vacancyId, studentId, null, null);

        Vacancy vacancy = new Vacancy();
        vacancy.setStatus(VacancyStatus.PUBLICADO);

        User studentUser = new User();
        studentUser.setStatus(AccountStatus.APROBADO);

        when(vacancyService.getVacancyById(vacancyId)).thenReturn(vacancy);
        when(studentProfileService.existsById(studentId)).thenReturn(true);
        when(userService.getById(studentId)).thenReturn(studentUser);
        when(educationService.getByStudentProfileId(studentId)).thenReturn(List.of());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.create(request));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(vacancyApplicationRepository, never()).save(any());
    }

    @Test
    void create_yaPostulado_lanzaDuplicateResource() {
        String vacancyId = "vac123456789";
        String studentId = "stu123456789";
        CreateVacancyApplicationRequest request =
                new CreateVacancyApplicationRequest(vacancyId, studentId, null, null);

        Vacancy vacancy = new Vacancy();
        vacancy.setStatus(VacancyStatus.PUBLICADO);

        User studentUser = new User();
        studentUser.setStatus(AccountStatus.APROBADO);

        when(vacancyService.getVacancyById(vacancyId)).thenReturn(vacancy);
        when(studentProfileService.existsById(studentId)).thenReturn(true);
        when(userService.getById(studentId)).thenReturn(studentUser);
        when(educationService.getByStudentProfileId(studentId)).thenReturn(List.of(new Education()));
        when(vacancyApplicationRepository.existsByVacancyIdAndStudentProfileId(vacancyId, studentId)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.create(request));
        verify(vacancyApplicationRepository, never()).save(any());
    }

    @Test
    void update_deVistoAPendiente_lanzaInvalidTransitionYNoGuarda() {
        String id = "app123456789";
        VacancyApplication application = new VacancyApplication();
        application.setStatus(VacancyApplicationStatus.VISTO);

        when(vacancyApplicationRepository.findById(id)).thenReturn(Optional.of(application));

        assertThrows(InvalidStatusTransitionException.class,
                () -> service.update(id, VacancyApplicationStatus.PENDIENTE));
        verify(vacancyApplicationRepository, never()).save(any());
    }

    @Test
    void update_dePendienteAVisto_guardaNuevoEstado() {
        String id = "app123456789";
        String vacancyId = "vac123456789";
        String studentId = "stu123456789";

        VacancyApplication application = new VacancyApplication();
        application.setStatus(VacancyApplicationStatus.PENDIENTE);
        application.setVacancyId(vacancyId);
        application.setStudentProfileId(studentId);

        when(vacancyApplicationRepository.findById(id)).thenReturn(Optional.of(application));
        when(vacancyApplicationRepository.save(any(VacancyApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VacancyApplication result = service.update(id, VacancyApplicationStatus.VISTO);

        assertEquals(VacancyApplicationStatus.VISTO, result.getStatus());
        verify(vacancyApplicationRepository).save(application);

        ArgumentCaptor<VacancyApplicationStatusChangedEvent> event =
                ArgumentCaptor.forClass(VacancyApplicationStatusChangedEvent.class);
        verify(eventPublisher).publishEvent(event.capture());
        assertEquals(vacancyId, event.getValue().vacancyId());
        assertEquals(studentId, event.getValue().studentProfileId());
        assertEquals(VacancyApplicationStatus.VISTO, event.getValue().newStatus());
    }

    @Test
    void update_deVistoAVisto_noLanzaExcepcion() {
        String id = "app123456789";
        VacancyApplication application = new VacancyApplication();
        application.setStatus(VacancyApplicationStatus.VISTO);

        when(vacancyApplicationRepository.findById(id)).thenReturn(Optional.of(application));
        when(vacancyApplicationRepository.save(any(VacancyApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VacancyApplication result = service.update(id, VacancyApplicationStatus.VISTO);

        assertEquals(VacancyApplicationStatus.VISTO, result.getStatus());
    }

    @Test
    void accept_seteaAcceptedTrueYGuarda() {
        String id = "app123456789";
        VacancyApplication application = new VacancyApplication();
        application.setAccepted(false);

        when(vacancyApplicationRepository.findById(id)).thenReturn(Optional.of(application));
        when(vacancyApplicationRepository.save(any(VacancyApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VacancyApplication result = service.accept(id);

        assertTrue(result.isAccepted());
        verify(vacancyApplicationRepository).save(application);
    }

    @Test
    void delete_inexistente_lanzaResourceNotFound() {
        String id = "app123456789";
        when(vacancyApplicationRepository.existsById(id)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.delete(id));
        verify(vacancyApplicationRepository, never()).deleteById(any());
    }

    @Test
    void countByStatusSummary_traeTodasLasClaves() {
        when(vacancyApplicationRepository.countByStatus(any())).thenReturn(0L);

        Map<VacancyApplicationStatus, Long> result = service.countByStatusSummary();

        assertEquals(VacancyApplicationStatus.values().length, result.size());
        for (VacancyApplicationStatus status : VacancyApplicationStatus.values()) {
            assertTrue(result.containsKey(status));
        }
    }
}