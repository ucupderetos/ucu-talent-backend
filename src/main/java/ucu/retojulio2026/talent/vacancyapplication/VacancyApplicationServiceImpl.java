package ucu.retojulio2026.talent.vacancyapplication;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.studentprofile.StudentProfileRepository;

import java.time.LocalDate;

@Service
public class VacancyApplicationServiceImpl implements VacancyApplicationService {

    private final VacancyApplicationRepository vacancyApplicationRepository;
    private final StudentProfileRepository studentProfileRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public VacancyApplicationServiceImpl(VacancyApplicationRepository vacancyApplicationRepository,
                                         StudentProfileRepository studentProfileRepository) {
        this.vacancyApplicationRepository = vacancyApplicationRepository;
        this.studentProfileRepository = studentProfileRepository;
    }

    @Override
    public VacancyApplication create(VacancyApplication vacancyApplication) {
        if (vacancyApplication == null) {
            throw new IllegalArgumentException("La vacancyApplication es obligatoria");
        }
        if (vacancyApplication.getVacancyId() == null || vacancyApplication.getVacancyId().isBlank()) {
            throw new IllegalArgumentException("El vacancyId es obligatorio");
        }
        if (vacancyApplication.getStudentProfileId() == null || vacancyApplication.getStudentProfileId().isBlank()) {
            throw new IllegalArgumentException("El studentProfileId es obligatorio");
        }

        if (!vacancyExists(vacancyApplication.getVacancyId())) {
            throw new ResourceNotFoundException("Vacancy con id '" + vacancyApplication.getVacancyId() + "' no encontrada");
        }
        if (!studentProfileRepository.existsById(vacancyApplication.getStudentProfileId())) {
            throw new ResourceNotFoundException("StudentProfile con id '" + vacancyApplication.getStudentProfileId() + "' no encontrado");
        }

        if (vacancyApplication.getStatus() == null) {
            vacancyApplication.setStatus(VacancyApplicationStatus.PENDIENTE);
        }
        if (vacancyApplication.getAppliedAt() == null) {
            vacancyApplication.setAppliedAt(LocalDate.now());
        }

        return vacancyApplicationRepository.save(vacancyApplication);
    }

    @Override
    public VacancyApplication getById(String id) {
        return vacancyApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VacancyApplication con id '" + id + "' no encontrada"));
    }

    @Override
    public VacancyApplication update(String id, VacancyApplicationStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("El status es obligatorio");
        }
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
