package ucu.retojulio2026.talent.Vacancy;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VacancyService implements IVacancyService {
    private final IVacancyRepository vacancyRepository;

    public VacancyService(IVacancyRepository vacancyRepository) {
        this.vacancyRepository = vacancyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vacancy> getAllVacancies() {
        return vacancyRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Vacancy getVacancyById(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid vacancy id.");
        }

        return vacancyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vacancy not found."));
    }

    @Override
    @Transactional
    public Vacancy saveVacancy(Vacancy vacancy) {

        if (vacancy == null) {
            throw new IllegalArgumentException("Vacancy cannot be null.");
        }

        return vacancyRepository.save(vacancy);
    }

    @Override
    @Transactional
    public Vacancy updateVacancy(Long id, Vacancy vacancy) {

        Vacancy existing = vacancyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vacancy not found."));

        existing.setPublicationDate(vacancy.getPublicationDate());
        existing.setClosingDate(vacancy.getClosingDate());
        existing.setLocality(vacancy.getLocality());
        existing.setModality(vacancy.getModality());
        existing.setStatus(vacancy.getStatus());
        existing.setName(vacancy.getName());
        existing.setDescription(vacancy.getDescription());
        existing.setRequirements(vacancy.getRequirements());
        existing.setContractType(vacancy.getContractType());
        existing.setSalaryRange(vacancy.getSalaryRange());

        // luego
        // existing.setCompany(vacancy.getCompany());
        // existing.setArea(vacancy.getArea());

        return vacancyRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteVacancy(Long id) {

        if (!vacancyRepository.existsById(id)) {
            throw new EntityNotFoundException("Vacancy not found.");
        }

        vacancyRepository.deleteById(id);
    }
}
