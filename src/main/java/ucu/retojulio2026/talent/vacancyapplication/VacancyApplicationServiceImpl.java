package ucu.retojulio2026.talent.vacancyapplication;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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
import ucu.retojulio2026.talent.vacancy.VacancyServiceImpl;
import ucu.retojulio2026.talent.vacancyapplication.dto.CreateVacancyApplicationRequest;
import ucu.retojulio2026.talent.vacancyapplication.dto.VacancyApplicationMapper;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class VacancyApplicationServiceImpl implements VacancyApplicationService {

    private final VacancyApplicationRepository vacancyApplicationRepository;
    private final StudentProfileService studentProfileService;
    private final EducationService educationService;
    private final VacancyApplicationMapper vacancyApplicationMapper;
    private final VacancyServiceImpl vacancyService;
    private final UserService userService;

    public VacancyApplicationServiceImpl(VacancyApplicationRepository vacancyApplicationRepository,
                                         StudentProfileService studentProfileService,
                                         EducationService educationService,
                                         VacancyApplicationMapper vacancyApplicationMapper,
                                         VacancyServiceImpl vacancyService,
                                         UserService userService) {
        this.vacancyApplicationRepository = vacancyApplicationRepository;
        this.studentProfileService = studentProfileService;
        this.educationService = educationService;
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
    public List<VacancyApplication> getByStudentProfileId(String studentProfileId) {
        return vacancyApplicationRepository.findByStudentProfileId(studentProfileId);
    }

    @Override
    public List<VacancyApplication> getByStatus(VacancyApplicationStatus status) {
        return vacancyApplicationRepository.findByStatus(status);
    }

    @Override
    public Map<VacancyApplicationStatus, Long> countByStatusSummary() {
        Map<VacancyApplicationStatus, Long> counts = new EnumMap<>(VacancyApplicationStatus.class);
        for (VacancyApplicationStatus status : VacancyApplicationStatus.values()) {
            counts.put(status, vacancyApplicationRepository.countByStatus(status));
        }
        return counts;
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
}
