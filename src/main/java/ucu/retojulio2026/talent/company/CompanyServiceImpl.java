package ucu.retojulio2026.talent.company;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import ucu.retojulio2026.talent.company.dto.CreateCompanyRequest;
import ucu.retojulio2026.talent.company.dto.PendingCompanyRow;
import ucu.retojulio2026.talent.company.dto.UpdateCompanyRequest;
import ucu.retojulio2026.talent.company.dto.CompanyMapper;
import ucu.retojulio2026.talent.common.DuplicateResourceException;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.user.AccountStatus;
import ucu.retojulio2026.talent.user.Role;
import ucu.retojulio2026.talent.user.User;
import ucu.retojulio2026.talent.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final UserService userService;

    public CompanyServiceImpl(CompanyRepository companyRepository, CompanyMapper companyMapper, UserService userService) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
        this.userService = userService;
    }

    @Override
    public Company create(String id, CreateCompanyRequest request) {
        if (!userService.existsById(id)) {
            throw new ResourceNotFoundException("User con id '" + id + "' no encontrado");
        }
        if (companyRepository.existsById(id)) {
            throw new DuplicateResourceException("El usuario '" + id + "' ya tiene una empresa asociada");
        }
        Company company = companyMapper.toEntity(id, request);
        return companyRepository.save(company);
    }

    @Override
    public Company getById(String id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company con id '" + id + "' no encontrada"));
    }

    @Override
    public List<Company> getAll(AccountStatus status) {
        if (status == null) {
            return companyRepository.findAll();
        }
        List<String> companyIds = userService.getAll(status, Role.EMPRESA, Pageable.unpaged())
                .getContent()
                .stream()
                .map(User::getUserId)
                .toList();
        return companyRepository.findAllById(companyIds);
    }


    @Override
    public Company update(String id, UpdateCompanyRequest request) {
        Company company = getById(id);
        company.setName(request.name());
        company.setIndustry(request.industry());
        company.setDescription(request.description());
        company.setWebUrl(request.webUrl());
        company.setLinkedinUrl(request.linkedinUrl());
        company.setLocation(request.location());
        return companyRepository.save(company);
    }

    @Override
    public void delete(String id) {
        if (!companyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Company con id '" + id + "' no encontrada");
        }
        companyRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return companyRepository.existsById(id);
    }

    @Override
    public Map<AccountStatus, Long> getStatusSummary() {
        return userService.countByRoleGroupedByStatus(Role.EMPRESA);
    }

    @Override
    public void review(String id, LocalDateTime reviewedAt, String adminComment) {
        Company company = getById(id);
        company.setReviewedAt(reviewedAt);
        company.setAdminComment(adminComment);
        companyRepository.save(company);
    }

    @Override
    public boolean hasProfile(String id) {
        return companyRepository.existsById(id);
    }


    @Override
    public long count() {
        return companyRepository.count();
    }

    @Override
    public long countByAccountStatus(AccountStatus status) {
        return companyRepository.countByAccountStatus(status);
    }

    @Override
    public List<PendingCompanyRow> getPendingForDashboard(int limit) {
        return companyRepository.findPendingForDashboard(PageRequest.of(0, limit));
    }
}
