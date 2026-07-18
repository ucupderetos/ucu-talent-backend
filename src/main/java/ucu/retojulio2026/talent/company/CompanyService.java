package ucu.retojulio2026.talent.company;

import ucu.retojulio2026.talent.company.dto.CreateCompanyRequest;
import ucu.retojulio2026.talent.company.dto.UpdateCompanyRequest;

import java.util.List;

public interface CompanyService {

    Company create(CreateCompanyRequest request);

    Company getById(String id);

    List<Company> getAll();

    Company getByUserId(String userId);

    Company update(String id, UpdateCompanyRequest request);

    void delete(String id);

    boolean existsById(String id);
}
