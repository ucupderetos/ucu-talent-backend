package ucu.retojulio2026.talent.vacancy;

import org.springframework.data.domain.Page;
import ucu.retojulio2026.talent.common.Department;
import ucu.retojulio2026.talent.vacancy.dto.CreateVacancyRequest;
import ucu.retojulio2026.talent.vacancy.dto.UpdateVacancyRequest;
import ucu.retojulio2026.talent.vacancy.dto.UpdateVacancyStatusAdminRequest;
import ucu.retojulio2026.talent.vacancy.dto.UpdateVacancyStatusRequest;
import ucu.retojulio2026.talent.vacancy.dto.SearchCriteriaVacancyRequest;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

public interface VacancyService {

    List<Vacancy> getAllVacancies();

    Map<VacancyStatus, Long> countByStatusSummary();

    Vacancy getVacancyById(String id);

    List<Vacancy> getByStatus(VacancyStatus status);

    List<Vacancy> getByCompanyId(String companyId);

    List<Vacancy> getByAreaId(String areaId);

    List<Vacancy> getByModality(Modality modality);

    List<Vacancy> getByLocation(Department location);

    Vacancy create(CreateVacancyRequest request);

    Vacancy updateVacancy(String id, UpdateVacancyRequest vacancy);

    void deleteVacancy(String id);

    boolean existsById(String id);

    Vacancy updateVacancyStatus(String id, UpdateVacancyStatusRequest request);

    Vacancy updateVacancyStatusAdmin(String id, String adminId, UpdateVacancyStatusAdminRequest vacancy);

    void finalizeExpiredVacancies();

    Page<Vacancy> search(SearchCriteriaVacancyRequest criteria, Pageable pageable);

    Page<Vacancy> searchPublished(SearchCriteriaVacancyRequest criteria, Pageable pageable);
}
