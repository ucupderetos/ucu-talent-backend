package ucu.retojulio2026.talent.vacancy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ucu.retojulio2026.talent.area.AreaService;
import ucu.retojulio2026.talent.common.AccountNotApprovedException;
import ucu.retojulio2026.talent.common.Department;
import ucu.retojulio2026.talent.common.ForbiddenOperationException;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.company.CompanyService;
import ucu.retojulio2026.talent.user.AccountStatus;
import ucu.retojulio2026.talent.user.User;
import ucu.retojulio2026.talent.user.UserService;
import ucu.retojulio2026.talent.vacancy.dto.CreateVacancyRequest;
import ucu.retojulio2026.talent.vacancy.dto.VacancyManagementResponse;
import ucu.retojulio2026.talent.vacancy.dto.VacancyManagementRow;
import ucu.retojulio2026.talent.vacancy.dto.VacancyMapper;
import ucu.retojulio2026.talent.vacancy.dto.VacancyResponse;
import ucu.retojulio2026.talent.vacancy.filter.VacancyFilterResolverImpl;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationRepository;
import ucu.retojulio2026.talent.vacancy.dto.UpdateVacancyRequest;
import ucu.retojulio2026.talent.vacancy.dto.UpdateVacancyStatusAdminRequest;
import ucu.retojulio2026.talent.vacancy.dto.UpdateVacancyStatusRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VacancyServiceImplTest {

    @Mock
    private VacancyRepository vacancyRepository;

    @Mock
    private VacancyMapper vacancyMapper;

    @Mock
    private CompanyService companyService;

    @Mock
    private AreaService areaService;

    @Mock
    private UserService userService;

    @Mock
    private VacancyApplicationRepository vacancyApplicationRepository;

    @Mock
    private VacancyFilterResolverImpl vacancyFilterResolverImpl;

    @Mock
    private VacancyFinalizationNotifier vacancyFinalizationNotifier;

    private VacancyServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new VacancyServiceImpl(
                vacancyRepository,
                vacancyMapper,
                companyService,
                areaService,
                userService,
                vacancyApplicationRepository,
                vacancyFilterResolverImpl,
                vacancyFinalizationNotifier
        );
    }

    private CreateVacancyRequest validRequest() {
        return new CreateVacancyRequest(
                "company-1",
                "area-1",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(10),
                Department.MONTEVIDEO,
                Modality.REMOTO,
                "Java Backend Developer",
                "Descripción",
                "Requisitos",
                ContractType.FULL_TIME,
                "USD 1000"
        );
    }

    private User companyUserWithStatus(AccountStatus status) {
        User user = new User();
        user.setUserId("company-1");
        user.setStatus(status);
        return user;
    }

    private Vacancy vacancyWithStatus(VacancyStatus status) {
        Vacancy vacancy = new Vacancy();
        vacancy.setVacancyId("vacancy-1");
        vacancy.setCompanyId("company-1");
        vacancy.setAreaId("area-1");
        vacancy.setPublicationDate(LocalDate.now().plusDays(1));
        vacancy.setClosingDate(LocalDate.now().plusDays(10));
        vacancy.setCreatedAt(LocalDateTime.now());
        vacancy.setLocation(Department.MONTEVIDEO);
        vacancy.setModality(Modality.REMOTO);
        vacancy.setStatus(status);
        vacancy.setName("Java Backend Developer");
        vacancy.setDescription("Descripción original");
        vacancy.setRequirements("Requisitos originales");
        vacancy.setContractType(ContractType.FULL_TIME);
        vacancy.setSalary("USD 1000");
        return vacancy;
    }

    private Vacancy publishedVacancy() {
        return vacancyWithStatus(VacancyStatus.PUBLICADO);
    }

    private Vacancy pendingVacancy() {
        return vacancyWithStatus(VacancyStatus.PENDIENTE);
    }

    private Vacancy finalizedVacancy() {
        return vacancyWithStatus(VacancyStatus.FINALIZADO);
    }

    private void mockExistingCompanyAndArea(Vacancy vacancy) {
        when(companyService.existsById(vacancy.getCompanyId())).thenReturn(true);
        when(areaService.existsById(vacancy.getAreaId())).thenReturn(true);
    }

    @Test
    void publicar_vacante_falla_si_la_empresa_no_existe() {
        CreateVacancyRequest request = validRequest();

        when(companyService.existsById("company-1")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.create(request));

        verify(companyService).existsById("company-1");
        verifyNoInteractions(userService);
        verifyNoInteractions(areaService);
        verifyNoInteractions(vacancyMapper);
        verify(vacancyRepository, never()).save(any(Vacancy.class));
    }

    @ParameterizedTest
    @EnumSource(value = AccountStatus.class, names = {"PENDIENTE", "RECHAZADO"})
    void publicar_vacante_falla_si_la_empresa_no_esta_aprobada(AccountStatus status) {
        CreateVacancyRequest request = validRequest();

        when(companyService.existsById("company-1")).thenReturn(true);
        when(userService.getById("company-1")).thenReturn(companyUserWithStatus(status));

        assertThrows(AccountNotApprovedException.class, () -> service.create(request));

        verify(companyService).existsById("company-1");
        verify(userService).getById("company-1");
        verifyNoInteractions(areaService);
        verifyNoInteractions(vacancyMapper);
        verify(vacancyRepository, never()).save(any(Vacancy.class));
    }

    @Test
    void publicar_vacante_falla_si_el_area_no_existe() {
        CreateVacancyRequest request = validRequest();

        when(companyService.existsById("company-1")).thenReturn(true);
        when(userService.getById("company-1")).thenReturn(companyUserWithStatus(AccountStatus.APROBADO));
        when(areaService.existsById("area-1")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.create(request));

        verify(companyService).existsById("company-1");
        verify(userService).getById("company-1");
        verify(areaService).existsById("area-1");
        verifyNoInteractions(vacancyMapper);
        verify(vacancyRepository, never()).save(any(Vacancy.class));
    }

    @Test
    void publicar_vacante_falla_si_la_fecha_de_publicacion_es_posterior_al_cierre() {
        CreateVacancyRequest request = new CreateVacancyRequest(
                "company-1",
                "area-1",
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(1),
                Department.MONTEVIDEO,
                Modality.REMOTO,
                "Java Backend Developer",
                "Descripción",
                "Requisitos",
                ContractType.FULL_TIME,
                "USD 1000"
        );

        when(companyService.existsById("company-1")).thenReturn(true);
        when(userService.getById("company-1")).thenReturn(companyUserWithStatus(AccountStatus.APROBADO));
        when(areaService.existsById("area-1")).thenReturn(true);

        assertThrows(ForbiddenOperationException.class, () -> service.create(request));

        verify(companyService).existsById("company-1");
        verify(userService).getById("company-1");
        verify(areaService).existsById("area-1");
        verifyNoInteractions(vacancyMapper);
        verify(vacancyRepository, never()).save(any(Vacancy.class));
    }

    @Test
    void publicar_vacante_con_empresa_y_area_inexistentes_falla_primero_por_empresa() {
        CreateVacancyRequest request = validRequest();

        when(companyService.existsById("company-1")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.create(request));

        verify(companyService).existsById("company-1");
        verifyNoInteractions(userService);
        verifyNoInteractions(areaService);
        verifyNoInteractions(vacancyMapper);
        verify(vacancyRepository, never()).save(any(Vacancy.class));
    }

    @Test
    void publicar_vacante_setea_fecha_de_creacion_al_guardar() {
        CreateVacancyRequest request = validRequest();

        when(companyService.existsById("company-1")).thenReturn(true);
        when(userService.getById("company-1")).thenReturn(companyUserWithStatus(AccountStatus.APROBADO));
        when(areaService.existsById("area-1")).thenReturn(true);

        Vacancy mappedVacancy = new Vacancy();
        mappedVacancy.setCompanyId("company-1");
        mappedVacancy.setAreaId("area-1");
        mappedVacancy.setPublicationDate(request.publicationDate());
        mappedVacancy.setClosingDate(request.closingDate());
        mappedVacancy.setLocation(request.location());
        mappedVacancy.setModality(request.modality());
        mappedVacancy.setName(request.name());
        mappedVacancy.setDescription(request.description());
        mappedVacancy.setRequirements(request.requirements());
        mappedVacancy.setContractType(request.contractType());
        mappedVacancy.setSalary(request.salary());

        when(vacancyMapper.toEntity(request)).thenReturn(mappedVacancy);
        when(vacancyRepository.save(any(Vacancy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.create(request);

        ArgumentCaptor<Vacancy> captor = ArgumentCaptor.forClass(Vacancy.class);
        verify(vacancyRepository).save(captor.capture());

        Vacancy captured = captor.getValue();

        assertNotNull(captured.getCreatedAt(), "createdAt debe estar seteado");

        LocalDateTime now = LocalDateTime.now(ZoneId.of("America/Montevideo"));

        assertTrue(
                captured.getCreatedAt().isAfter(now.minusSeconds(5))
                        && captured.getCreatedAt().isBefore(now.plusSeconds(1)),
                "createdAt debe estar dentro de una ventana razonable"
        );

        verify(companyService).existsById("company-1");
        verify(userService).getById("company-1");
        verify(areaService).existsById("area-1");
        verify(vacancyMapper).toEntity(request);
    }

    @Test
    void resumen_de_puestos_por_estado_incluye_todos_los_estados_aunque_el_conteo_sea_cero() {
        when(vacancyRepository.countByStatus(VacancyStatus.PENDIENTE)).thenReturn(0L);
        when(vacancyRepository.countByStatus(VacancyStatus.PUBLICADO)).thenReturn(3L);
        when(vacancyRepository.countByStatus(VacancyStatus.FINALIZADO)).thenReturn(1L);

        Map<VacancyStatus, Long> summary = service.countByStatusSummary();

        assertEquals(3, summary.size());
        assertEquals(0L, summary.get(VacancyStatus.PENDIENTE));
        assertEquals(3L, summary.get(VacancyStatus.PUBLICADO));
        assertEquals(1L, summary.get(VacancyStatus.FINALIZADO));

        verify(vacancyRepository).countByStatus(VacancyStatus.PENDIENTE);
        verify(vacancyRepository).countByStatus(VacancyStatus.PUBLICADO);
        verify(vacancyRepository).countByStatus(VacancyStatus.FINALIZADO);
    }

    @Test
    void editar_vacante_falla_si_no_existe() {
        UpdateVacancyRequest request = new UpdateVacancyRequest(
                null,
                null,
                null,
                null,
                "Nuevo nombre",
                null,
                null,
                null,
                null
        );

        when(vacancyRepository.findById("vacancy-1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.updateVacancy("vacancy-1", request));

        verify(vacancyRepository).findById("vacancy-1");
        verifyNoInteractions(companyService);
        verifyNoInteractions(areaService);
        verifyNoInteractions(vacancyApplicationRepository);
        verify(vacancyRepository, never()).save(any(Vacancy.class));
    }

    @Test
    void editar_vacante_falla_si_tiene_postulaciones() {
        Vacancy existing = publishedVacancy();

        UpdateVacancyRequest request = new UpdateVacancyRequest(
                null,
                null,
                null,
                null,
                "Nuevo nombre",
                null,
                null,
                null,
                null
        );

        when(vacancyRepository.findById("vacancy-1")).thenReturn(Optional.of(existing));
        mockExistingCompanyAndArea(existing);
        when(vacancyApplicationRepository.existsByVacancyId("vacancy-1")).thenReturn(true);

        assertThrows(ForbiddenOperationException.class, () -> service.updateVacancy("vacancy-1", request));

        verify(vacancyRepository).findById("vacancy-1");
        verify(companyService).existsById("company-1");
        verify(areaService).existsById("area-1");
        verify(vacancyApplicationRepository).existsByVacancyId("vacancy-1");
        verify(vacancyRepository, never()).save(any(Vacancy.class));
    }

    @Test
    void editar_vacante_falla_si_esta_finalizada() {
        Vacancy existing = finalizedVacancy();

        UpdateVacancyRequest request = new UpdateVacancyRequest(
                null,
                null,
                null,
                null,
                "Nuevo nombre",
                null,
                null,
                null,
                null
        );

        when(vacancyRepository.findById("vacancy-1")).thenReturn(Optional.of(existing));
        mockExistingCompanyAndArea(existing);
        when(vacancyApplicationRepository.existsByVacancyId("vacancy-1")).thenReturn(false);

        assertThrows(ForbiddenOperationException.class, () -> service.updateVacancy("vacancy-1", request));

        verify(vacancyRepository).findById("vacancy-1");
        verify(companyService).existsById("company-1");
        verify(areaService).existsById("area-1");
        verify(vacancyApplicationRepository).existsByVacancyId("vacancy-1");
        verify(vacancyRepository, never()).save(any(Vacancy.class));
    }

    @Test
    void edicion_parcial_no_pisa_datos_existentes_con_null_y_actualiza_los_no_null() {
        Vacancy existing = publishedVacancy();

        LocalDate originalPublicationDate = existing.getPublicationDate();
        LocalDate originalClosingDate = existing.getClosingDate();
        Department originalLocation = existing.getLocation();
        Modality originalModality = existing.getModality();
        String originalDescription = existing.getDescription();
        ContractType originalContractType = existing.getContractType();

        UpdateVacancyRequest request = new UpdateVacancyRequest(
                null,
                null,
                null,
                null,
                "Nuevo nombre",
                null,
                "Nuevos requisitos",
                null,
                "USD 2000"
        );

        when(vacancyRepository.findById("vacancy-1")).thenReturn(Optional.of(existing));
        mockExistingCompanyAndArea(existing);
        when(vacancyApplicationRepository.existsByVacancyId("vacancy-1")).thenReturn(false);
        when(vacancyRepository.save(any(Vacancy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vacancy updated = service.updateVacancy("vacancy-1", request);

        assertEquals(originalPublicationDate, updated.getPublicationDate());
        assertEquals(originalClosingDate, updated.getClosingDate());
        assertEquals(originalLocation, updated.getLocation());
        assertEquals(originalModality, updated.getModality());
        assertEquals(originalDescription, updated.getDescription());
        assertEquals(originalContractType, updated.getContractType());

        assertEquals("Nuevo nombre", updated.getName());
        assertEquals("Nuevos requisitos", updated.getRequirements());
        assertEquals("USD 2000", updated.getSalary());
        assertNotNull(updated.getUpdatedAt());

        verify(vacancyRepository).save(existing);
    }

    @Test
    void editar_vacante_falla_si_las_fechas_resultantes_son_inconsistentes() {
        Vacancy existing = publishedVacancy();
        existing.setPublicationDate(LocalDate.now().plusDays(1));
        existing.setClosingDate(LocalDate.now().plusDays(10));

        UpdateVacancyRequest request = new UpdateVacancyRequest(
                LocalDate.now().plusDays(20),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(vacancyRepository.findById("vacancy-1")).thenReturn(Optional.of(existing));
        mockExistingCompanyAndArea(existing);
        when(vacancyApplicationRepository.existsByVacancyId("vacancy-1")).thenReturn(false);

        assertThrows(ForbiddenOperationException.class, () -> service.updateVacancy("vacancy-1", request));

        verify(vacancyRepository).findById("vacancy-1");
        verify(companyService).existsById("company-1");
        verify(areaService).existsById("area-1");
        verify(vacancyApplicationRepository).existsByVacancyId("vacancy-1");
        verify(vacancyRepository, never()).save(any(Vacancy.class));
    }

    @Test
    void dar_de_baja_vacante_falla_si_esta_finalizada() {
        Vacancy existing = finalizedVacancy();

        when(vacancyRepository.findById("vacancy-1")).thenReturn(Optional.of(existing));

        assertThrows(ForbiddenOperationException.class, () -> service.deleteVacancy("vacancy-1"));

        verify(vacancyRepository).findById("vacancy-1");
        verify(vacancyRepository, never()).save(any(Vacancy.class));
        verify(vacancyRepository, never()).deleteById(anyString());
    }

    @Test
    void dar_de_baja_vacante_es_logica_y_no_fisica() {
        Vacancy existing = publishedVacancy();

        when(vacancyRepository.findById("vacancy-1")).thenReturn(Optional.of(existing));

        service.deleteVacancy("vacancy-1");

        assertTrue(existing.isDeleted());
        assertNotNull(existing.getDeletedAt());
        assertEquals(VacancyStatus.FINALIZADO, existing.getStatus());

        verify(vacancyRepository).findById("vacancy-1");
        verify(vacancyRepository).save(existing);
        verify(vacancyRepository, never()).deleteById(anyString());
    }

    @Test
    void cerrar_vacante_falla_si_ya_esta_finalizada() {
        Vacancy existing = finalizedVacancy();
        UpdateVacancyStatusRequest request = new UpdateVacancyStatusRequest(VacancyStatus.FINALIZADO);

        when(vacancyRepository.findById("vacancy-1")).thenReturn(Optional.of(existing));

        assertThrows(ForbiddenOperationException.class, () -> service.updateVacancyStatus("vacancy-1", request));

        verify(vacancyRepository).findById("vacancy-1");
        verify(vacancyRepository, never()).save(any(Vacancy.class));
        verifyNoInteractions(vacancyFinalizationNotifier);
    }

    @Test
    void cerrar_vacante_falla_si_esta_pendiente_de_revision() {
        Vacancy existing = pendingVacancy();
        UpdateVacancyStatusRequest request = new UpdateVacancyStatusRequest(VacancyStatus.FINALIZADO);

        when(vacancyRepository.findById("vacancy-1")).thenReturn(Optional.of(existing));

        assertThrows(ForbiddenOperationException.class, () -> service.updateVacancyStatus("vacancy-1", request));

        verify(vacancyRepository).findById("vacancy-1");
        verify(vacancyRepository, never()).save(any(Vacancy.class));
        verifyNoInteractions(vacancyFinalizationNotifier);
    }

    @Test
    void admin_no_puede_cambiar_estado_de_vacante_finalizada() {
        Vacancy existing = finalizedVacancy();

        UpdateVacancyStatusAdminRequest request = new UpdateVacancyStatusAdminRequest(
                "Comentario admin",
                VacancyStatus.PUBLICADO
        );

        when(vacancyRepository.findById("vacancy-1")).thenReturn(Optional.of(existing));

        assertThrows(ForbiddenOperationException.class,
                () -> service.updateVacancyStatusAdmin("vacancy-1", "admin-1", request));

        verify(vacancyRepository).findById("vacancy-1");
        verify(vacancyRepository, never()).save(any(Vacancy.class));
        verifyNoInteractions(vacancyFinalizationNotifier);
    }

    @Test
    void admin_al_cambiar_estado_deja_rastro_de_revision() {
        Vacancy existing = pendingVacancy();

        UpdateVacancyStatusAdminRequest request = new UpdateVacancyStatusAdminRequest(
                "Puesto aprobado por admin",
                VacancyStatus.PUBLICADO
        );

        when(vacancyRepository.findById("vacancy-1")).thenReturn(Optional.of(existing));
        when(vacancyRepository.save(any(Vacancy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vacancy updated = service.updateVacancyStatusAdmin("vacancy-1", "admin-1", request);

        assertEquals("admin-1", updated.getReviewedBy());
        assertEquals("Puesto aprobado por admin", updated.getAdminComment());
        assertNotNull(updated.getReviewedAt());
        assertEquals(VacancyStatus.PUBLICADO, updated.getStatus());

        verify(vacancyRepository).findById("vacancy-1");
        verify(vacancyRepository).save(existing);
        verifyNoInteractions(vacancyFinalizationNotifier);
    }

    @Test
    void cierre_automatico_finaliza_vacantes_publicadas_vencidas_y_no_toca_las_no_vencidas() {
        LocalDate today = LocalDate.now(ZoneId.of("America/Montevideo"));

        Vacancy expiredVacancy = publishedVacancy();
        expiredVacancy.setVacancyId("expired-1");
        expiredVacancy.setClosingDate(today.minusDays(1));

        Vacancy expiresTodayVacancy = publishedVacancy();
        expiresTodayVacancy.setVacancyId("expired-2");
        expiresTodayVacancy.setClosingDate(today);

        Vacancy notExpiredVacancy = publishedVacancy();
        notExpiredVacancy.setVacancyId("not-expired-1");
        notExpiredVacancy.setClosingDate(today.plusDays(1));

        when(vacancyRepository.findByStatusAndClosingDateLessThanEqual(VacancyStatus.PUBLICADO, today))
                .thenReturn(List.of(expiredVacancy, expiresTodayVacancy));

        service.finalizeExpiredVacancies();

        assertEquals(VacancyStatus.FINALIZADO, expiredVacancy.getStatus());
        assertEquals(VacancyStatus.FINALIZADO, expiresTodayVacancy.getStatus());
        assertEquals(VacancyStatus.PUBLICADO, notExpiredVacancy.getStatus());

        verify(vacancyRepository).findByStatusAndClosingDateLessThanEqual(VacancyStatus.PUBLICADO, today);
        verify(vacancyFinalizationNotifier).notifyApplicants(expiredVacancy);
        verify(vacancyFinalizationNotifier).notifyApplicants(expiresTodayVacancy);
        verify(vacancyRepository, never()).save(any(Vacancy.class));
    }

    private VacancyResponse responseOf(Vacancy vacancy) {
        return new VacancyResponse(
                vacancy.getVacancyId(),
                vacancy.getCompanyId(),
                vacancy.getAreaId(),
                vacancy.getPublicationDate(),
                vacancy.getClosingDate(),
                vacancy.getCreatedAt(),
                vacancy.getReviewedAt(),
                vacancy.getUpdatedAt(),
                vacancy.getDeletedAt(),
                vacancy.isDeleted(),
                vacancy.getAdminComment(),
                vacancy.getLocation(),
                vacancy.getModality(),
                vacancy.getStatus(),
                vacancy.getName(),
                vacancy.getDescription(),
                vacancy.getRequirements(),
                vacancy.getContractType(),
                vacancy.getReviewedBy(),
                vacancy.getSalary()
        );
    }

    private VacancyManagementResponse managementResponseOf(VacancyManagementRow row) {
        return new VacancyManagementResponse(
                responseOf(row.vacancy()),
                row.companyName(),
                row.areaName(),
                row.applicationCount(),
                row.newApplicationsCount()
        );
    }

    @Test
    void admin_al_finalizar_una_vacante_finaliza_tambien_sus_postulaciones() {
        Vacancy existing = publishedVacancy();
        UpdateVacancyStatusAdminRequest request = new UpdateVacancyStatusAdminRequest(
                "Cerrado por incumplir las normas", VacancyStatus.FINALIZADO);

        when(vacancyRepository.findById("vacancy-1")).thenReturn(Optional.of(existing));
        when(vacancyRepository.save(any(Vacancy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateVacancyStatusAdmin("vacancy-1", "admin-1", request);

        verify(vacancyApplicationRepository).finalizeByVacancyId("vacancy-1");
        verify(vacancyFinalizationNotifier).notifyApplicants(existing);
    }

    @Test
    void admin_al_publicar_una_vacante_no_toca_las_postulaciones() {
        Vacancy existing = pendingVacancy();
        UpdateVacancyStatusAdminRequest request = new UpdateVacancyStatusAdminRequest(
                "Puesto aprobado por admin", VacancyStatus.PUBLICADO);

        when(vacancyRepository.findById("vacancy-1")).thenReturn(Optional.of(existing));
        when(vacancyRepository.save(any(Vacancy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateVacancyStatusAdmin("vacancy-1", "admin-1", request);

        verify(vacancyApplicationRepository, never()).finalizeByVacancyId(anyString());
        verifyNoInteractions(vacancyFinalizationNotifier);
    }

    @Test
    void dar_de_baja_vacante_se_comporta_como_un_cierre_y_finaliza_sus_postulaciones() {
        Vacancy existing = publishedVacancy();

        when(vacancyRepository.findById("vacancy-1")).thenReturn(Optional.of(existing));
        when(vacancyRepository.save(any(Vacancy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.deleteVacancy("vacancy-1");

        verify(vacancyApplicationRepository).finalizeByVacancyId("vacancy-1");
        verify(vacancyFinalizationNotifier).notifyApplicants(existing);
    }

    @Test
    void dar_de_baja_vacante_ya_finalizada_no_toca_las_postulaciones_ni_avisa() {
        Vacancy existing = finalizedVacancy();

        when(vacancyRepository.findById("vacancy-1")).thenReturn(Optional.of(existing));

        assertThrows(ForbiddenOperationException.class, () -> service.deleteVacancy("vacancy-1"));

        verify(vacancyApplicationRepository, never()).finalizeByVacancyId(anyString());
        verifyNoInteractions(vacancyFinalizationNotifier);
    }

    @Test
    void cerrar_vacante_a_mano_finaliza_tambien_sus_postulaciones() {
        Vacancy existing = publishedVacancy();
        UpdateVacancyStatusRequest request = new UpdateVacancyStatusRequest(VacancyStatus.FINALIZADO);

        when(vacancyRepository.findById("vacancy-1")).thenReturn(Optional.of(existing));
        when(vacancyRepository.save(any(Vacancy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateVacancyStatus("vacancy-1", request);

        verify(vacancyApplicationRepository).finalizeByVacancyId("vacancy-1");
        verify(vacancyFinalizationNotifier).notifyApplicants(existing);
    }

    @Test
    void cerrar_vacante_a_mano_a_un_estado_que_no_es_finalizado_no_toca_las_postulaciones() {
        Vacancy existing = publishedVacancy();
        UpdateVacancyStatusRequest request = new UpdateVacancyStatusRequest(VacancyStatus.PUBLICADO);

        when(vacancyRepository.findById("vacancy-1")).thenReturn(Optional.of(existing));
        when(vacancyRepository.save(any(Vacancy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateVacancyStatus("vacancy-1", request);

        verify(vacancyApplicationRepository, never()).finalizeByVacancyId(anyString());
        verifyNoInteractions(vacancyFinalizationNotifier);
    }

    @Test
    void cierre_automatico_finaliza_las_postulaciones_de_cada_vacante_vencida() {
        LocalDate today = LocalDate.now(ZoneId.of("America/Montevideo"));

        Vacancy expiredVacancy = publishedVacancy();
        expiredVacancy.setVacancyId("expired-1");
        expiredVacancy.setClosingDate(today.minusDays(1));

        Vacancy expiresTodayVacancy = publishedVacancy();
        expiresTodayVacancy.setVacancyId("expired-2");
        expiresTodayVacancy.setClosingDate(today);

        when(vacancyRepository.findByStatusAndClosingDateLessThanEqual(VacancyStatus.PUBLICADO, today))
                .thenReturn(List.of(expiredVacancy, expiresTodayVacancy));

        service.finalizeExpiredVacancies();

        verify(vacancyApplicationRepository).finalizeByVacancyId("expired-1");
        verify(vacancyApplicationRepository).finalizeByVacancyId("expired-2");
    }

    @Test
    void cierre_automatico_sin_vacantes_vencidas_no_toca_ninguna_postulacion() {
        LocalDate today = LocalDate.now(ZoneId.of("America/Montevideo"));

        when(vacancyRepository.findByStatusAndClosingDateLessThanEqual(VacancyStatus.PUBLICADO, today))
                .thenReturn(List.of());

        service.finalizeExpiredVacancies();

        verify(vacancyApplicationRepository, never()).finalizeByVacancyId(anyString());
        verifyNoInteractions(vacancyFinalizationNotifier);
    }

    @Test
    void tablero_de_gestion_falla_si_la_empresa_no_existe() {
        when(companyService.existsById("company-1")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.getManagementByCompanyId("company-1"));

        verify(companyService).existsById("company-1");
        verifyNoInteractions(vacancyRepository);
        verifyNoInteractions(vacancyMapper);
    }

    @Test
    void tablero_de_gestion_devuelve_contadores_y_nombres_resueltos() {
        Vacancy vacancy = publishedVacancy();
        VacancyManagementRow row = new VacancyManagementRow(vacancy, "ACME S.A.", "Desarrollo de Software", 5L, 2L);

        when(companyService.existsById("company-1")).thenReturn(true);
        when(vacancyRepository.findManagementByCompanyId("company-1")).thenReturn(List.of(row));
        when(vacancyMapper.toManagementResponse(row)).thenReturn(managementResponseOf(row));

        VacancyManagementResponse result = service.getManagementByCompanyId("company-1").get(0);

        assertEquals("ACME S.A.", result.companyName());
        assertEquals("Desarrollo de Software", result.areaName());
        assertEquals(5L, result.applicationCount());
        assertEquals(2L, result.newApplicationsCount());
        assertEquals("vacancy-1", result.vacancy().vacancyId());
        assertEquals("Java Backend Developer", result.vacancy().name());
        assertEquals(VacancyStatus.PUBLICADO, result.vacancy().status());
    }

    @Test
    void tablero_de_gestion_de_puesto_sin_postulaciones_devuelve_contadores_en_cero() {
        Vacancy vacancy = publishedVacancy();
        VacancyManagementRow row = new VacancyManagementRow(vacancy, "ACME S.A.", "Desarrollo de Software", 0L, 0L);

        when(companyService.existsById("company-1")).thenReturn(true);
        when(vacancyRepository.findManagementByCompanyId("company-1")).thenReturn(List.of(row));
        when(vacancyMapper.toManagementResponse(row)).thenReturn(managementResponseOf(row));

        VacancyManagementResponse result = service.getManagementByCompanyId("company-1").get(0);

        assertEquals(0L, result.applicationCount());
        assertEquals(0L, result.newApplicationsCount());
        assertEquals("vacancy-1", result.vacancy().vacancyId());
    }

    @Test
    void tablero_de_gestion_mapea_cada_fila_y_preserva_el_orden_del_repositorio() {
        Vacancy reciente = publishedVacancy();
        reciente.setVacancyId("vacancy-reciente");

        Vacancy antigua = publishedVacancy();
        antigua.setVacancyId("vacancy-antigua");

        VacancyManagementRow primera = new VacancyManagementRow(reciente, "ACME S.A.", "Desarrollo de Software", 3L, 1L);
        VacancyManagementRow segunda = new VacancyManagementRow(antigua, "ACME S.A.", "Desarrollo de Software", 0L, 0L);

        when(companyService.existsById("company-1")).thenReturn(true);
        when(vacancyRepository.findManagementByCompanyId("company-1")).thenReturn(List.of(primera, segunda));
        when(vacancyMapper.toManagementResponse(primera)).thenReturn(managementResponseOf(primera));
        when(vacancyMapper.toManagementResponse(segunda)).thenReturn(managementResponseOf(segunda));

        List<VacancyManagementResponse> result = service.getManagementByCompanyId("company-1");

        assertEquals(2, result.size());
        assertEquals("vacancy-reciente", result.get(0).vacancy().vacancyId());
        assertEquals("vacancy-antigua", result.get(1).vacancy().vacancyId());
        verify(vacancyRepository).findManagementByCompanyId("company-1");
    }

    @Test
    void tablero_de_gestion_de_empresa_sin_puestos_devuelve_lista_vacia() {
        when(companyService.existsById("company-1")).thenReturn(true);
        when(vacancyRepository.findManagementByCompanyId("company-1")).thenReturn(List.of());

        List<VacancyManagementResponse> result = service.getManagementByCompanyId("company-1");

        assertTrue(result.isEmpty());
        verify(vacancyMapper, never()).toManagementResponse(any());
    }
}