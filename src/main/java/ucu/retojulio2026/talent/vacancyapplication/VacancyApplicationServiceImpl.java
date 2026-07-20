package ucu.retojulio2026.talent.vacancyapplication;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import ucu.retojulio2026.talent.common.DuplicateResourceException;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.studentprofile.StudentProfileRepository;
import ucu.retojulio2026.talent.vacancy.VacancyServiceImpl;
import ucu.retojulio2026.talent.vacancyapplication.dto.CreateVacancyApplicationRequest;
import ucu.retojulio2026.talent.vacancyapplication.dto.VacancyApplicationMapper;

import java.util.List;

@Service
public class VacancyApplicationServiceImpl implements VacancyApplicationService {

    private final VacancyApplicationRepository vacancyApplicationRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final VacancyApplicationMapper vacancyApplicationMapper;
    private final VacancyServiceImpl vacancyService;
    @PersistenceContext
    private EntityManager entityManager;

    public VacancyApplicationServiceImpl(VacancyApplicationRepository vacancyApplicationRepository,
                                         StudentProfileRepository studentProfileRepository, VacancyApplicationMapper vacancyApplicationMapper, VacancyServiceImpl vacancyService) {
        this.vacancyApplicationRepository = vacancyApplicationRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.vacancyApplicationMapper = vacancyApplicationMapper;
        this.vacancyService = vacancyService;
    }

    @Override
    public VacancyApplication create(CreateVacancyApplicationRequest request) {
        if (!vacancyExists(request.vacancyId())) {
            throw new ResourceNotFoundException("Vacancy con id '" + request.vacancyId() + "' no encontrada");
        }
        if (!studentProfileRepository.existsById(request.studentProfileId())) {
            throw new ResourceNotFoundException("StudentProfile con id '" + request.studentProfileId() + "' no encontrado");
        }
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
