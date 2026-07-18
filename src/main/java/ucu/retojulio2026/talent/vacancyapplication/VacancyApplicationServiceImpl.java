package ucu.retojulio2026.talent.vacancyapplication;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.studentprofile.StudentProfileRepository;
import ucu.retojulio2026.talent.vacancyapplication.dto.CreateVacancyApplicationRequest;
import ucu.retojulio2026.talent.vacancyapplication.dto.VacancyApplicationMapper;

import java.time.LocalDate;

@Service
public class VacancyApplicationServiceImpl implements VacancyApplicationService {

    private final VacancyApplicationRepository vacancyApplicationRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final VacancyApplicationMapper vacancyApplicationMapper;

    @PersistenceContext
    private EntityManager entityManager;

    public VacancyApplicationServiceImpl(VacancyApplicationRepository vacancyApplicationRepository,
                                         StudentProfileRepository studentProfileRepository, VacancyApplicationMapper vacancyApplicationMapper) {
        this.vacancyApplicationRepository = vacancyApplicationRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.vacancyApplicationMapper = vacancyApplicationMapper;
    }

    @Override
    public VacancyApplication create(CreateVacancyApplicationRequest request) {
        VacancyApplication vacancyApplication = vacancyApplicationMapper.toEntity(request);
        return vacancyApplicationRepository.save(vacancyApplication);
    }

    @Override
    public VacancyApplication getById(String id) {
        return vacancyApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VacancyApplication con id '" + id + "' no encontrada"));
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
        return !entityManager.createNativeQuery("SELECT 1 FROM vacancy WHERE vacancy_id = :vacancyId LIMIT 1")
                .setParameter("vacancyId", vacancyId)
                .getResultList()
                .isEmpty();
    }

}
