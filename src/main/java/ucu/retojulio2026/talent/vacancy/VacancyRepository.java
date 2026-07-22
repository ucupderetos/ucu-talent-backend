package ucu.retojulio2026.talent.vacancy;

import org.springframework.data.jpa.repository.JpaRepository;
import ucu.retojulio2026.talent.common.Department;

import java.time.LocalDate;
import java.util.List;

public interface VacancyRepository extends JpaRepository<Vacancy, String> {
    List<Vacancy> findByStatus(VacancyStatus status);
    List<Vacancy> findByCompanyId(String companyId);
    List<Vacancy> findByAreaId(String areaId);
    List<Vacancy> findByModality(Modality modality);
    List<Vacancy> findByLocation(Department location);
    List<Vacancy> findByStatusAndClosingDateLessThanEqual(VacancyStatus status, LocalDate closingDateIsLessThan);
    long countByStatus(VacancyStatus status);
}
