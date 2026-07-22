package ucu.retojulio2026.talent.vacancy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ucu.retojulio2026.talent.common.Department;

import java.time.LocalDate;
import java.util.List;

public interface VacancyRepository extends JpaRepository<Vacancy, String>, JpaSpecificationExecutor<Vacancy> {
    List<Vacancy> findByStatus(VacancyStatus status);
    List<Vacancy> findByCompanyId(String companyId);
    List<Vacancy> findByAreaId(String areaId);
    List<Vacancy> findByModality(Modality modality);
    List<Vacancy> findByLocation(Department location);
    List<Vacancy> findByStatusAndClosingDateLessThanEqual(VacancyStatus status, LocalDate closingDateIsLessThan);
}