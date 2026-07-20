package ucu.retojulio2026.talent.company;

import org.springframework.stereotype.Service;

import ucu.retojulio2026.talent.company.dto.CreateCompanyRequest;
import ucu.retojulio2026.talent.company.dto.UpdateCompanyRequest;
import ucu.retojulio2026.talent.company.dto.CompanyMapper;
import ucu.retojulio2026.talent.common.DuplicateResourceException;
import ucu.retojulio2026.talent.common.ResourceNotFoundException;
import ucu.retojulio2026.talent.user.UserService;

import java.util.List;

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
    public Company create(CreateCompanyRequest request) {
        if (!userService.existsById(request.userId())) {
            throw new ResourceNotFoundException("User con id '" + request.userId() + "' no encontrado");
        }
        // companyId es siempre igual a userId (PK compartida): si ya existe, repository.save(...)
        // haria un UPDATE silencioso (merge) en vez de fallar, porque el id ya viene seteado.
        if (companyRepository.existsById(request.userId())) {
            throw new DuplicateResourceException("El usuario '" + request.userId() + "' ya tiene una empresa asociada");
        }
        Company company = companyMapper.toEntity(request);
        company.setApproved(false);
        return companyRepository.save(company);
    }

    @Override
    public Company getById(String id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company con id '" + id + "' no encontrada"));
    }

    @Override
    public List<Company> getAll() {
        return companyRepository.findAll();
    }

    @Override
    public Company getByUserId(String userId) {
        return companyRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Company con userId '" + userId + "' no encontrada"));
    }

    @Override
    public Company update(String id, UpdateCompanyRequest request) {
        Company company = getById(id);
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

}

