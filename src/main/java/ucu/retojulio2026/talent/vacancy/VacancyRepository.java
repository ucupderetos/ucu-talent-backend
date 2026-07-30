package ucu.retojulio2026.talent.vacancy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ucu.retojulio2026.talent.common.Department;
import ucu.retojulio2026.talent.vacancy.dto.VacancyManagementRow;

import java.time.LocalDate;
import java.util.List;

public interface VacancyRepository extends JpaRepository<Vacancy, String>, JpaSpecificationExecutor<Vacancy> {

    @Query("""
            SELECT new ucu.retojulio2026.talent.vacancy.dto.VacancyManagementRow(
                v,
                c.name,
                ar.name,
                COUNT(a.vacancyApplicationId),
                COALESCE(SUM(CASE WHEN a.status = ucu.retojulio2026.talent.vacancyapplication.VacancyApplicationStatus.PENDIENTE THEN 1L ELSE 0L END), 0L)
            )
            FROM Vacancy v
            JOIN Company c ON c.companyId = v.companyId
            JOIN Area ar ON ar.areaId = v.areaId
            LEFT JOIN VacancyApplication a ON a.vacancyId = v.vacancyId
            WHERE v.companyId = :companyId AND v.deleted = false
            GROUP BY v, c.name, ar.name
            ORDER BY v.createdAt DESC
            """)
    List<VacancyManagementRow> findManagementByCompanyId(@Param("companyId") String companyId);

    List<Vacancy> findByStatus(VacancyStatus status);
    List<Vacancy> findByCompanyId(String companyId);
    List<Vacancy> findByAreaId(String areaId);
    List<Vacancy> findByModality(Modality modality);
    List<Vacancy> findByLocation(Department location);
    List<Vacancy> findByStatusAndClosingDateLessThanEqual(VacancyStatus status, LocalDate closingDateIsLessThan);
    long countByStatus(VacancyStatus status);
}
