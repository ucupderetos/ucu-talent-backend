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
import ucu.retojulio2026.talent.vacancyapplication.dto.ApplicationListItemResponse;
import ucu.retojulio2026.talent.vacancyapplication.dto.ApplicationListItemRow;
import ucu.retojulio2026.talent.vacancyapplication.dto.CreateVacancyApplicationRequest;
import ucu.retojulio2026.talent.vacancyapplication.dto.VacancyApplicationMapper;
import ucu.retojulio2026.talent.vacancyapplication.dto.VacancyApplicationResponse;

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

    private VacancyApplication application(String id, String vacancyId, String studentId) {
        VacancyApplication application = new VacancyApplication();
        application.setVacancyApplicationId(id);
        application.setVacancyId(vacancyId);
        application.setStudentProfileId(studentId);
        application.setStatus(VacancyApplicationStatus.PENDIENTE);
        application.setAppliedAt(LocalDate.now());
        return application;
    }

    private ApplicationListItemRow row(VacancyApplication application, String studentName, String studentEmail,
                                       String vacancyName, String companyId, String companyName) {
        return new ApplicationListItemRow(application, studentName, "Perez", studentEmail,
                application.getVacancyId(), vacancyName, companyId, companyName);
    }

    private ApplicationListItemResponse response(ApplicationListItemRow row) {
        VacancyApplication application = row.application();
        return new ApplicationListItemResponse(
                new VacancyApplicationResponse(
                        application.getVacancyApplicationId(),
                        application.getVacancyId(),
                        application.getStudentProfileId(),
                        application.getStatus(),
                        application.getAppliedAt(),
                        application.isAccepted()),
                row.studentName(), row.studentSurname(), row.studentEmail(),
                row.vacancyId(), row.vacancyName(), row.companyId(), row.companyName());
    }

    @Test
    void getDetailedByVacancyId_mapeaCadaFilaYPreservaElOrdenDelRepositorio() {
        String vacancyId = "vac123456789";
        ApplicationListItemRow primera = row(application("app111111111", vacancyId, "stu111111111"),
                "Ana", "ana@correo.ucu.edu.uy", "Backend Developer", "cmp123456789", "ACME S.A.");
        ApplicationListItemRow segunda = row(application("app222222222", vacancyId, "stu222222222"),
                "Bruno", "bruno@correo.ucu.edu.uy", "Backend Developer", "cmp123456789", "ACME S.A.");

        when(vacancyApplicationRepository.findDetailedByVacancyId(vacancyId))
                .thenReturn(List.of(primera, segunda));
        when(vacancyApplicationMapper.toListItemResponse(primera)).thenReturn(response(primera));
        when(vacancyApplicationMapper.toListItemResponse(segunda)).thenReturn(response(segunda));

        List<ApplicationListItemResponse> result = service.getDetailedByVacancyId(vacancyId);

        assertEquals(2, result.size());
        assertEquals("app111111111", result.get(0).application().vacancyApplicationId());
        assertEquals("app222222222", result.get(1).application().vacancyApplicationId());
        verify(vacancyApplicationRepository).findDetailedByVacancyId(vacancyId);
    }

    @Test
    void getDetailedByVacancyId_devuelveAlumnoVacanteYEmpresaResueltos() {
        String vacancyId = "vac123456789";
        ApplicationListItemRow fila = row(application("app111111111", vacancyId, "stu111111111"),
                "Ana", "ana@correo.ucu.edu.uy", "Backend Developer", "cmp123456789", "ACME S.A.");

        when(vacancyApplicationRepository.findDetailedByVacancyId(vacancyId)).thenReturn(List.of(fila));
        when(vacancyApplicationMapper.toListItemResponse(fila)).thenReturn(response(fila));

        ApplicationListItemResponse result = service.getDetailedByVacancyId(vacancyId).get(0);

        assertEquals("Ana", result.studentName());
        assertEquals("Perez", result.studentSurname());
        assertEquals("ana@correo.ucu.edu.uy", result.studentEmail());
        assertEquals(vacancyId, result.vacancyId());
        assertEquals("Backend Developer", result.vacancyName());
        assertEquals("cmp123456789", result.companyId());
        assertEquals("ACME S.A.", result.companyName());
        assertEquals("stu111111111", result.application().studentProfileId());
        assertEquals(VacancyApplicationStatus.PENDIENTE, result.application().status());
    }

    @Test
    void getDetailedByVacancyId_sinPostulaciones_devuelveListaVaciaYNoUsaElMapper() {
        String vacancyId = "vac123456789";
        when(vacancyApplicationRepository.findDetailedByVacancyId(vacancyId)).thenReturn(List.of());

        List<ApplicationListItemResponse> result = service.getDetailedByVacancyId(vacancyId);

        assertTrue(result.isEmpty());
        verify(vacancyApplicationMapper, never()).toListItemResponse(any());
    }

    @Test
    void getAllDetailed_devuelveFilasDeDistintasEmpresasSinFiltrar() {
        ApplicationListItemRow deAcme = row(application("app111111111", "vac111111111", "stu111111111"),
                "Ana", "ana@correo.ucu.edu.uy", "Backend Developer", "cmp111111111", "ACME S.A.");
        ApplicationListItemRow deOtra = row(application("app222222222", "vac222222222", "stu222222222"),
                "Bruno", "bruno@correo.ucu.edu.uy", "Data Analyst", "cmp222222222", "OTRA S.R.L.");

        when(vacancyApplicationRepository.findAllDetailed()).thenReturn(List.of(deAcme, deOtra));
        when(vacancyApplicationMapper.toListItemResponse(deAcme)).thenReturn(response(deAcme));
        when(vacancyApplicationMapper.toListItemResponse(deOtra)).thenReturn(response(deOtra));

        List<ApplicationListItemResponse> result = service.getAllDetailed();

        assertEquals(2, result.size());
        assertEquals("ACME S.A.", result.get(0).companyName());
        assertEquals("OTRA S.R.L.", result.get(1).companyName());
        verify(vacancyApplicationRepository).findAllDetailed();
    }

    @Test
    void getAllDetailed_sinPostulaciones_devuelveListaVaciaYNoUsaElMapper() {
        when(vacancyApplicationRepository.findAllDetailed()).thenReturn(List.of());

        List<ApplicationListItemResponse> result = service.getAllDetailed();

        assertTrue(result.isEmpty());
        verify(vacancyApplicationMapper, never()).toListItemResponse(any());
    }
}