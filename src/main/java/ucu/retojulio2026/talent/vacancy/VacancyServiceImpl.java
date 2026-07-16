package ucu.retojulio2026.talent.vacancy;

import ucu.retojulio2026.talent.vacancy.dto.CreateVacancyRequest;
import ucu.retojulio2026.talent.vacancy.dto.VacancyMapper;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VacancyServiceImpl implements VacancyService {
    private final IVacancyRepository vacancyRepository;
    private final VacancyMapper vacancyMapper;

    public VacancyServiceImpl(IVacancyRepository vacancyRepository, VacancyMapper vacancyMapper) {
        this.vacancyRepository = vacancyRepository;
        this.vacancyMapper = vacancyMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vacancy> getAllVacancies() {
        return vacancyRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Vacancy getVacancyById(String id) {

        return vacancyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found."));
    }

    @Override
    @Transactional
    public Vacancy create(CreateVacancyRequest request) {
        Vacancy vacancy = vacancyMapper.toEntity(request);
        vacancy.setStatus(VacancyStatus.PENDIENTE);
        return vacancyRepository.save(vacancy);
    }

    @Override
    @Transactional
    public Vacancy updateVacancy(String id, CreateVacancyRequest request) {
        Vacancy existing = vacancyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found."));

        Vacancy updated = vacancyMapper.toEntity(request);

        existing.setPublicationDate(updated.getPublicationDate());
        existing.setClosingDate(updated.getClosingDate());
        existing.setLocation(updated.getLocation());
        existing.setModality(updated.getModality());
        existing.setStatus(updated.getStatus());
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setRequirements(updated.getRequirements());
        existing.setContractType(updated.getContractType());
        existing.setSalaryRange(updated.getSalaryRange());

        // luego
        // existing.setCompany(updated.getCompany());
        // existing.setArea(updated.getArea());

        return vacancyRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteVacancy(String id) {

        Vacancy existing = vacancyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found."));

        vacancyRepository.deleteById(id);
    }
}
