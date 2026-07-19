package ucu.retojulio2026.talent.vacancy;

import ucu.retojulio2026.talent.area.Area;
import ucu.retojulio2026.talent.area.AreaService;
import ucu.retojulio2026.talent.company.Company;
import ucu.retojulio2026.talent.company.CompanyService;
import ucu.retojulio2026.talent.vacancy.dto.CreateVacancyRequest;
import ucu.retojulio2026.talent.vacancy.dto.VacancyMapper;
import ucu.retojulio2026.talent.common.CompanyNotApprovedException;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VacancyServiceImpl implements VacancyService {
    private final IVacancyRepository vacancyRepository;
    private final VacancyMapper vacancyMapper;
    private final CompanyService companyService;
    private final AreaService areaService;

    public VacancyServiceImpl(IVacancyRepository vacancyRepository, VacancyMapper vacancyMapper, CompanyService companyService, AreaService areaService) {
        this.vacancyRepository = vacancyRepository;
        this.vacancyMapper = vacancyMapper;
        this.companyService = companyService;
        this.areaService = areaService;
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
    @Transactional(readOnly = true)
    public List<Vacancy> getByStatus(VacancyStatus status) {
        return vacancyRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vacancy> getByCompanyId(String companyId) {
        return vacancyRepository.findByCompanyId(companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vacancy> getByAreaId(String areaId) {
        return vacancyRepository.findByAreaId(areaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vacancy> getByModality(Modality modality) {
        return vacancyRepository.findByModality(modality);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vacancy> getByLocation(Departamento location) {
        return vacancyRepository.findByLocation(location);
    }

    // Fresco de la base en cada llamada, nunca del JWT: approved es estado mutable (un ADMIN
    // puede pasarlo a false en cualquier momento) y el token puede seguir siendo valido hasta
    // 60 min despues de ese cambio. Ver learning/Auth/2026-07-18-jwt-stateless-auth-design.md, seccion 10.
    private void requireApprovedCompany(String companyId) {
        Company company = companyService.getById(companyId);
        if (!company.getApproved()) {
            throw new CompanyNotApprovedException();
        }
    }

    @Override
    @Transactional
    public Vacancy create(CreateVacancyRequest request) {
        if (!companyService.existsById(request.companyId())) {
            throw new ResourceNotFoundException("Company not found.");
        }
        requireApprovedCompany(request.companyId());
        if (!areaService.existsById(request.areaId())) {
            throw new ResourceNotFoundException("Area not found.");
        }
        Vacancy vacancy = vacancyMapper.toEntity(request);
        vacancy.setStatus(VacancyStatus.PENDIENTE);
        return vacancyRepository.save(vacancy);
    }

    @Override
    @Transactional
    public Vacancy updateVacancy(String id, CreateVacancyRequest request) {
        if (!companyService.existsById(request.companyId())) {
            throw new ResourceNotFoundException("Company not found.");
        }
        requireApprovedCompany(request.companyId());
        if (!areaService.existsById(request.areaId())) {
            throw new ResourceNotFoundException("Area not found.");
        }
        Vacancy existing = vacancyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found."));

        Vacancy updated = vacancyMapper.toEntity(request);

        // publicationDate no se toca: se asigna una sola vez al crear (@CreationTimestamp,
        // updatable = false en la entidad) - no es un campo editable via PUT.
        existing.setClosingDate(updated.getClosingDate());
        existing.setLocation(updated.getLocation());
        existing.setModality(updated.getModality());
        // status es opcional en el DTO: si no vino en el request, no se pisa el valor actual
        // (evita el NOT NULL de la columna si el cliente no lo manda).
        if (updated.getStatus() != null) {
            existing.setStatus(updated.getStatus());
        }
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setRequirements(updated.getRequirements());
        existing.setContractType(updated.getContractType());
        existing.setSalaryRange(updated.getSalaryRange());
        existing.setCompanyId(updated.getCompanyId());
        existing.setAreaId(updated.getAreaId());

        return vacancyRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteVacancy(String id) {

        Vacancy existing = vacancyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found."));

        vacancyRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return vacancyRepository.existsById(id);
    }
}
