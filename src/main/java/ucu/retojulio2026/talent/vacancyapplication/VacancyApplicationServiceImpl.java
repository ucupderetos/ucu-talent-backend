package ucu.retojulio2026.talent.vacancyapplication;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ucu.retojulio2026.talent.common.AccountNotApprovedException;
import ucu.retojulio2026.talent.common.DuplicateResourceException;
import ucu.retojulio2026.talent.common.InvalidStatusTransitionException;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.education.EducationService;
import ucu.retojulio2026.talent.studentprofile.StudentProfileService;
import ucu.retojulio2026.talent.user.AccountStatus;
import ucu.retojulio2026.talent.user.UserService;
import ucu.retojulio2026.talent.vacancy.Vacancy;
import ucu.retojulio2026.talent.vacancy.VacancyStatus;
import ucu.retojulio2026.talent.vacancy.VacancyService;
import ucu.retojulio2026.talent.vacancyapplication.dto.ApplicationListItemResponse;
import ucu.retojulio2026.talent.vacancyapplication.dto.CreateVacancyApplicationRequest;
import ucu.retojulio2026.talent.vacancyapplication.dto.MyApplicationRowResponse;
import ucu.retojulio2026.talent.vacancyapplication.dto.VacancyApplicationMapper;
import ucu.retojulio2026.talent.vacancyapplication.dto.VacancyApplicationStudentResponse;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class    VacancyApplicationServiceImpl implements VacancyApplicationService {

    private final VacancyApplicationRepository vacancyApplicationRepository;
    private final StudentProfileService studentProfileService;
    private final EducationService educationService;
    private final VacancyApplicationMapper vacancyApplicationMapper;
    private final VacancyService vacancyService;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    public VacancyApplicationServiceImpl(VacancyApplicationRepository vacancyApplicationRepository,
                                         StudentProfileService studentProfileService,
                                         EducationService educationService,
                                         VacancyApplicationMapper vacancyApplicationMapper,
                                         VacancyService vacancyService,
                                         UserService userService,
                                         ApplicationEventPublisher eventPublisher) {
        this.vacancyApplicationRepository = vacancyApplicationRepository;
        this.studentProfileService = studentProfileService;
        this.educationService = educationService;
        this.vacancyApplicationMapper = vacancyApplicationMapper;
        this.vacancyService = vacancyService;
        this.userService = userService;
        this.eventPublisher = eventPublisher;
    }


    private void requireApprovedStudent(String studentProfileId) {
        if (userService.getById(studentProfileId).getStatus() == AccountStatus.RECHAZADO) {
            throw new AccountNotApprovedException("No se puede postular porque tu cuenta está en estado RECHAZADO");
        }
        if (userService.getById(studentProfileId).getStatus() != AccountStatus.APROBADO) {
            throw new AccountNotApprovedException("No se puede postular porque tu cuenta no está aprobada");
        }
    }

    @Override
    @Transactional
    public VacancyApplication create(CreateVacancyApplicationRequest request) {
        Vacancy vacancy = vacancyService.getVacancyById(request.vacancyId());

        if (vacancy.getStatus() != VacancyStatus.PUBLICADO) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se puede postular a vacantes en estado PUBLICADO"
            );
        }

        if (!studentProfileService.existsById(request.studentProfileId())) {
            throw new ResourceNotFoundException("StudentProfile con id '" + request.studentProfileId() + "' no encontrado");
        }

        requireApprovedStudent(request.studentProfileId());

        if (educationService.getByStudentProfileId(request.studentProfileId()).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El alumno debe tener al menos un registro de educacion para postularse"
            );
        }

        if (vacancyApplicationRepository.existsByVacancyIdAndStudentProfileId(request.vacancyId(), request.studentProfileId())) {
            throw new DuplicateResourceException("El alumno '" + request.studentProfileId()
                    + "' ya se postuló a la vacante '" + request.vacancyId() + "'");
        }

        VacancyApplication vacancyApplication = vacancyApplicationMapper.toEntity(request);
        vacancyApplication.setStatus(VacancyApplicationStatus.PENDIENTE);
        vacancyApplication.setAppliedAt(LocalDate.now());
        VacancyApplication created = vacancyApplicationRepository.save(vacancyApplication);

        eventPublisher.publishEvent(new VacancyApplicationCreatedEvent(
                created.getVacancyId(), created.getStudentProfileId()));

        return created;
    }

    @Override
    @Transactional(readOnly = true)
    public VacancyApplication getById(String id) {
        return vacancyApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VacancyApplication con id '" + id + "' no encontrada"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VacancyApplication> getAll() {
        return vacancyApplicationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VacancyApplication> getByVacancyId(String vacancyId) {
        return vacancyApplicationRepository.findByVacancyId(vacancyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VacancyApplication> getByStudentProfileId(String studentProfileId) {
        return vacancyApplicationRepository.findByStudentProfileId(studentProfileId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VacancyApplicationStudentResponse> getStudentApplications(String studentProfileId) {
        return vacancyApplicationRepository.findStudentApplications(studentProfileId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyApplicationRowResponse> getMyApplicationsDetailed(String studentProfileId) {
        return vacancyApplicationRepository.findMyApplicationsDetailed(studentProfileId)
                .stream()
                .map(vacancyApplicationMapper::toMyApplicationRowResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationListItemResponse> getDetailedByVacancyId(String vacancyId) {
        return vacancyApplicationRepository.findDetailedByVacancyId(vacancyId)
                .stream()
                .map(vacancyApplicationMapper::toListItemResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationListItemResponse> getAllDetailed() {
        return vacancyApplicationRepository.findAllDetailed()
                .stream()
                .map(vacancyApplicationMapper::toListItemResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VacancyApplication> getByStatus(VacancyApplicationStatus status) {
        return vacancyApplicationRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<VacancyApplicationStatus, Long> countByStatusSummary() {
        Map<VacancyApplicationStatus, Long> counts = new EnumMap<>(VacancyApplicationStatus.class);
        for (VacancyApplicationStatus status : VacancyApplicationStatus.values()) {
            counts.put(status, vacancyApplicationRepository.countByStatus(status));
        }
        return counts;
    }

    @Override
    @Transactional
    public VacancyApplication update(String id, VacancyApplicationStatus status) {
        VacancyApplication vacancyApplication = getById(id);
        VacancyApplicationStatus previousStatus = vacancyApplication.getStatus();
        if (status.ordinal() < vacancyApplication.getStatus().ordinal()) {
            throw new InvalidStatusTransitionException(
                    "No se puede retroceder de '" + vacancyApplication.getStatus() + "' a '" + status + "'");
        }
        vacancyApplication.setStatus(status);
        VacancyApplication updated = vacancyApplicationRepository.save(vacancyApplication);

        if (previousStatus != status) {
            eventPublisher.publishEvent(new VacancyApplicationStatusChangedEvent(
                    updated.getVacancyId(), updated.getStudentProfileId(), status));
        }

        return updated;
    }

    @Override
    @Transactional
    public VacancyApplication accept(String id) {
        VacancyApplication vacancyApplication = getById(id);
        vacancyApplication.setAccepted(true);
        return vacancyApplicationRepository.save(vacancyApplication);
    }

    @Override
    @Transactional
    public void delete(String id) {
        if (!vacancyApplicationRepository.existsById(id)) {
            throw new ResourceNotFoundException("VacancyApplication con id '" + id + "' no encontrada");
        }
        vacancyApplicationRepository.deleteById(id);
    }
}
