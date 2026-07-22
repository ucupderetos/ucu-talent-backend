package ucu.retojulio2026.talent.vacancy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.server.ResponseStatusException;
import ucu.retojulio2026.talent.area.AreaService;
import ucu.retojulio2026.talent.audit.Auditable;
import ucu.retojulio2026.talent.common.Department;
import ucu.retojulio2026.talent.common.ForbiddenOperationException;
import ucu.retojulio2026.talent.company.Company;
import ucu.retojulio2026.talent.company.CompanyService;
import ucu.retojulio2026.talent.user.AccountStatus;
import ucu.retojulio2026.talent.user.User;
import ucu.retojulio2026.talent.user.UserService;
import ucu.retojulio2026.talent.vacancy.dto.*;
import ucu.retojulio2026.talent.common.AccountNotApprovedException;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import ucu.retojulio2026.talent.vacancy.filter.VacancyFilterResolverImpl;
import ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class VacancyServiceImpl implements VacancyService {
    private final VacancyRepository vacancyRepository;
    private final VacancyMapper vacancyMapper;
    private final CompanyService companyService;
    private final UserService userService;
    private final AreaService areaService;
    private final VacancyApplicationRepository vacancyApplicationRepository;
    private final VacancyFilterResolverImpl vacancyFilterResolverImpl;


    public VacancyServiceImpl(VacancyRepository vacancyRepository, VacancyMapper vacancyMapper, CompanyService companyService, AreaService areaService, UserService userService, VacancyApplicationRepository vacancyApplicationRepository, VacancyFilterResolverImpl vacancyFilterResolverImpl) {
        this.vacancyRepository = vacancyRepository;
        this.vacancyMapper = vacancyMapper;
        this.companyService = companyService;
        this.areaService = areaService;
        this.userService = userService;
        this.vacancyApplicationRepository = vacancyApplicationRepository;
        this.vacancyFilterResolverImpl = vacancyFilterResolverImpl;
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
    public List<Vacancy> getByLocation(Department location) {
        return vacancyRepository.findByLocation(location);
    }

    private void requireApprovedCompany(String companyId) {
        if (userService.getById(companyId).getStatus() != AccountStatus.APROBADO) {
            throw new AccountNotApprovedException();
        }
    }

    @Override
    @Transactional
    public Vacancy create(CreateVacancyRequest request) {
        if (!companyService.existsById(request.companyId())) {
            throw new ResourceNotFoundException("Company not found.");
        }
        User existingUser = userService.getById(request.companyId());
        if (existingUser.getStatus() != AccountStatus.APROBADO){
            throw new AccountNotApprovedException();
        }
        if (!areaService.existsById(request.areaId())) {
            throw new ResourceNotFoundException("Area not found.");
        }
        if (request.publicationDate().isAfter(request.closingDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha de publicación no puede ser posterior a la fecha de cierre."
            );
        }
        Vacancy vacancy = vacancyMapper.toEntity(request);
        vacancy.setCreatedAt(LocalDateTime.now(ZoneId.of("America/Montevideo"))); // No guarda adecuadamente la hora si no especifico la zona.
        return vacancyRepository.save(vacancy);
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "America/Montevideo")
    @Transactional
    public void finalizeExpiredVacancies() {
        LocalDate today = LocalDate.now(ZoneId.of("America/Montevideo"));

        List<Vacancy> expired = vacancyRepository
                .findByStatusAndClosingDateLessThanEqual((VacancyStatus.PUBLICADO), today);

        for (Vacancy vacancy : expired) {
            vacancy.setStatus(VacancyStatus.FINALIZADO);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Vacancy> search(SearchCriteriaVacancyRequest criteria, Pageable pageable) {
        Specification<Vacancy> specification = vacancyFilterResolverImpl.buildSpecification(criteria);
        return vacancyRepository.findAll(specification, pageable);
    }

    @Override
    @Transactional
    public Vacancy updateVacancy(String id, UpdateVacancyRequest request) {

        Vacancy existing = vacancyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found."));

        if (!companyService.existsById(existing.getCompanyId())) {
            throw new ResourceNotFoundException("Company not found.");
        }

        if (!areaService.existsById(existing.getAreaId())) {
            throw new ResourceNotFoundException("Area not found.");
        }

        if (vacancyApplicationRepository.existsByVacancyId(id)) {
            throw new ForbiddenOperationException(
                    "El Puesto ya tiene postulaciones."
            );
        }

        if (existing.getStatus() == VacancyStatus.FINALIZADO) {
            throw new ForbiddenOperationException(
                    "El Puesto ya finalizó."
            );
        }

        if (request.publicationDate() != null) {
            existing.setPublicationDate(request.publicationDate());
        }
        if (request.closingDate() != null) {
            existing.setClosingDate(request.closingDate());
        }
        if (request.location() != null) {
            existing.setLocation(request.location());
        }
        if (request.modality() != null) {
            existing.setModality(request.modality());
        }
        if (request.name() != null) {
            existing.setName(request.name());
        }
        if (request.description() != null) {
            existing.setDescription(request.description());
        }
        if (request.requirements() != null) {
            existing.setRequirements(request.requirements());
        }
        if (request.contractType() != null) {
            existing.setContractType(request.contractType());
        }
        if (request.salaryRange() != null) {
            existing.setSalaryRange(request.salaryRange());
        }

        if (existing.getPublicationDate().isAfter(existing.getClosingDate())) {
            throw new ForbiddenOperationException(
                    "La fecha de publicación no puede ser posterior a la fecha de cierre."
            );
        }

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

    @Auditable(module = "VACANCY", action = "VACANCY_STATUS_UPDATE", entityId = "#id")
    @Override
    @Transactional
    public Vacancy updateVacancyStatusAdmin(String id, String adminId, UpdateVacancyStatusAdminRequest request) {
        Vacancy existing = vacancyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found."));
        if (existing.getStatus() == VacancyStatus.FINALIZADO) {
            throw new ForbiddenOperationException(
                    "El Puesto ya finalizó."
            );
        }
        if (existing.getStatus() == VacancyStatus.PENDIENTE) {
            throw new ForbiddenOperationException(
                    "El Puesto está en revisión."
            );
        }
        existing.setReviewedBy(adminId);
        existing.setStatus(request.status());
        existing.setAdminComment(request.adminComment()); // Si no queda un comentario de otro, da igual si manda null
        existing.setReviewedAt(LocalDateTime.now(ZoneId.of("America/Montevideo"))); // No guarda adecuadamente la hora si no especifico la zona.

        return vacancyRepository.save(existing);
    }

    @Override
    @Transactional
    public Vacancy updateVacancyStatus(String id, UpdateVacancyStatusRequest request) {
        Vacancy existing = vacancyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found."));
        if (existing.getStatus() == VacancyStatus.FINALIZADO) {
            throw new ForbiddenOperationException(
                    "El Puesto ya finalizó."
            );
        }
        if (existing.getStatus() == VacancyStatus.PENDIENTE) {
            throw new ForbiddenOperationException(
                    "El Puesto está en revisión."
            );
        }
        existing.setStatus(request.status());

        return vacancyRepository.save(existing);
    }
}