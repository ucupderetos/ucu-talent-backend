package ucu.retojulio2026.talent.vacancyapplication;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import ucu.retojulio2026.talent.common.AccountNotApprovedException;
import ucu.retojulio2026.talent.common.DuplicateResourceException;
import ucu.retojulio2026.talent.common.InvalidStatusTransitionException;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.studentprofile.StudentProfileService;
import ucu.retojulio2026.talent.user.AccountStatus;
import ucu.retojulio2026.talent.user.UserService;
import ucu.retojulio2026.talent.vacancy.VacancyServiceImpl;
import ucu.retojulio2026.talent.vacancyapplication.dto.CreateVacancyApplicationRequest;
import ucu.retojulio2026.talent.vacancyapplication.dto.VacancyApplicantResponse;
import ucu.retojulio2026.talent.vacancyapplication.dto.VacancyApplicationMapper;

import java.util.List;

@Service
public class VacancyApplicationServiceImpl implements VacancyApplicationService {

    private final VacancyApplicationRepository vacancyApplicationRepository;
    private final StudentProfileService studentProfileService;
    private final VacancyApplicationMapper vacancyApplicationMapper;
    private final VacancyServiceImpl vacancyService;
    private final UserService userService;
    @PersistenceContext
    private EntityManager entityManager;

    public VacancyApplicationServiceImpl(VacancyApplicationRepository vacancyApplicationRepository,
                                         StudentProfileService studentProfileService,
                                         VacancyApplicationMapper vacancyApplicationMapper,
                                         VacancyServiceImpl vacancyService,
                                         UserService userService) {
        this.vacancyApplicationRepository = vacancyApplicationRepository;
        this.studentProfileService = studentProfileService;
        this.vacancyApplicationMapper = vacancyApplicationMapper;
        this.vacancyService = vacancyService;
        this.userService = userService;
    }


    private void requireApprovedStudent(String studentProfileId) {
        if (userService.getById(studentProfileId).getStatus() != AccountStatus.APROBADO) {
            throw new AccountNotApprovedException();
        }
    }

    @Override
    public VacancyApplication create(CreateVacancyApplicationRequest request) {
        if (!vacancyExists(request.vacancyId())) {
            throw new ResourceNotFoundException("Vacancy con id '" + request.vacancyId() + "' no encontrada");
        }
        if (!studentProfileService.existsById(request.studentProfileId())) {
            throw new ResourceNotFoundException("StudentProfile con id '" + request.studentProfileId() + "' no encontrado");
        }
        requireApprovedStudent(request.studentProfileId());
        if (vacancyApplicationRepository.existsByVacancyIdAndStudentProfileId(request.vacancyId(), request.studentProfileId())) {
            throw new DuplicateResourceException("El alumno '" + request.studentProfileId()
                    + "' ya se postuló a la vacante '" + request.vacancyId() + "'");
        }
        VacancyApplication vacancyApplication = vacancyApplicationMapper.toEntity(request);
        return vacancyApplicationRepository.save(vacancyApplication);
    }

    @Override
    public VacancyApplication getById(String id) {
        return vacancyApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VacancyApplication con id '" + id + "' no encontrada"));
    }

    @Override
    public List<VacancyApplication> getAll() {
        return vacancyApplicationRepository.findAll();
    }

    @Override
    public List<VacancyApplication> getByVacancyId(String vacancyId) {
        return vacancyApplicationRepository.findByVacancyId(vacancyId);
    }

    @Override
    public List<VacancyApplicantResponse> getApplicantsByVacancyId(String vacancyId) {
        return vacancyApplicationRepository.findByVacancyId(vacancyId).stream()
                .map(application -> vacancyApplicationMapper.toApplicantResponse(
                        application, studentProfileService.getById(application.getStudentProfileId())))
                .toList();
    }

    @Override
    public List<VacancyApplication> getByStudentProfileId(String studentProfileId) {
        return vacancyApplicationRepository.findByStudentProfileId(studentProfileId);
    }

    @Override
    public List<VacancyApplication> getByStatus(VacancyApplicationStatus status) {
        return vacancyApplicationRepository.findByStatus(status);
    }

    @Override
    public VacancyApplication update(String id, VacancyApplicationStatus status) {
        VacancyApplication vacancyApplication = getById(id);
        if (status.ordinal() < vacancyApplication.getStatus().ordinal()) {
            throw new InvalidStatusTransitionException(
                    "No se puede retroceder de '" + vacancyApplication.getStatus() + "' a '" + status + "'");
        }
        vacancyApplication.setStatus(status);
        return vacancyApplicationRepository.save(vacancyApplication);
    }

    @Override
    public void delete(String id) {
        if (!vacancyApplicationRepository.existsById(id)) {
            throw new ResourceNotFoundException("VacancyApplication con id '" + id + "' no encontrada");
        }
        vacancyApplicationRepository.deleteById(id);
    }
    private boolean vacancyExists(String vacancyId) {
        return vacancyService.existsById(vacancyId);
    }

}
