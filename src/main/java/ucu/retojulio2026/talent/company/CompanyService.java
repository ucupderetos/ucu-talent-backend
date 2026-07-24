package ucu.retojulio2026.talent.company;

import ucu.retojulio2026.talent.company.dto.CreateCompanyRequest;
import ucu.retojulio2026.talent.company.dto.UpdateCompanyRequest;
import ucu.retojulio2026.talent.user.AccountStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface CompanyService {

    Company create(String id, CreateCompanyRequest request);

    Company getById(String id);

    List<Company> getAll();

    Company update(String id, UpdateCompanyRequest request);

    void delete(String id);

    boolean existsById(String id);

    Map<AccountStatus, Long> getStatusSummary();

    void review(String id, LocalDateTime reviewedAt, String adminComment);

    boolean hasProfile(String id);
}
