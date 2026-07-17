package ucu.retojulio2026.talent.company;

import ucu.retojulio2026.talent.company.dto.CreateCompanyRequest;
import ucu.retojulio2026.talent.company.dto.UpdateCompanyRequest;

public interface CompanyService {

    Company create(CreateCompanyRequest request);

    Company getById(String id);

    Company getByUserId(String userId);

    Company update(String id, UpdateCompanyRequest request);

    void delete(String id);

    boolean existsById(String id);
}
