package ucu.retojulio2026.talent.vacancyapplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ucu.retojulio2026.talent.vacancyapplication.dto.ApplicationListItemRow;
import ucu.retojulio2026.talent.vacancyapplication.dto.MyApplicationRow;
import ucu.retojulio2026.talent.vacancyapplication.dto.VacancyApplicationStudentResponse;

import java.util.List;

@Repository
public interface VacancyApplicationRepository extends JpaRepository<VacancyApplication, String> {


    @Query("""
            SELECT new ucu.retojulio2026.talent.vacancyapplication.dto.VacancyApplicationStudentResponse(
                a.vacancyApplicationId,
                a.vacancyId,
                v.name,
                v.companyId,
                c.name,
                a.appliedAt,
                a.status,
                v.status
            )
            FROM VacancyApplication a
            JOIN Vacancy v ON v.vacancyId = a.vacancyId
            JOIN Company c ON c.companyId = v.companyId
            WHERE a.studentProfileId = :studentProfileId
            ORDER BY a.appliedAt DESC
            """)
    List<VacancyApplicationStudentResponse> findStudentApplications(@Param("studentProfileId") String studentProfileId);

    @Query("""
            SELECT new ucu.retojulio2026.talent.vacancyapplication.dto.ApplicationListItemRow(
                a,
                sp.name,
                sp.surname,
                u.email,
                v.vacancyId,
                v.name,
                c.companyId,
                c.name
            )
            FROM VacancyApplication a
            JOIN StudentProfile sp ON sp.studentProfileId = a.studentProfileId
            JOIN User u ON u.userId = a.studentProfileId
            JOIN Vacancy v ON v.vacancyId = a.vacancyId
            JOIN Company c ON c.companyId = v.companyId
            WHERE a.vacancyId = :vacancyId
            ORDER BY a.appliedAt DESC
            """)
    List<ApplicationListItemRow> findDetailedByVacancyId(@Param("vacancyId") String vacancyId);

    @Query("""
            SELECT new ucu.retojulio2026.talent.vacancyapplication.dto.ApplicationListItemRow(
                a,
                sp.name,
                sp.surname,
                u.email,
                v.vacancyId,
                v.name,
                c.companyId,
                c.name
            )
            FROM VacancyApplication a
            JOIN StudentProfile sp ON sp.studentProfileId = a.studentProfileId
            JOIN User u ON u.userId = a.studentProfileId
            JOIN Vacancy v ON v.vacancyId = a.vacancyId
            JOIN Company c ON c.companyId = v.companyId
            ORDER BY a.appliedAt DESC
            """)
    List<ApplicationListItemRow> findAllDetailed();

    @Query("""
            SELECT new ucu.retojulio2026.talent.vacancyapplication.dto.MyApplicationRow(
                a,
                v,
                c.name,
                ar.name
            )
            FROM VacancyApplication a
            JOIN Vacancy v ON v.vacancyId = a.vacancyId
            JOIN Company c ON c.companyId = v.companyId
            JOIN Area ar ON ar.areaId = v.areaId
            WHERE a.studentProfileId = :studentProfileId
            ORDER BY a.appliedAt DESC
            """)
    List<MyApplicationRow> findMyApplicationsDetailed(@Param("studentProfileId") String studentProfileId);

    List<VacancyApplication> findByVacancyId(String vacancyId);

    List<VacancyApplication> findByStudentProfileId(String studentProfileId);

    List<VacancyApplication> findByStatus(VacancyApplicationStatus status);

    boolean existsByVacancyIdAndStudentProfileId(String vacancyId, String studentProfileId);

    boolean existsByVacancyId(String vacancyId);

    long countByStatus(VacancyApplicationStatus status);
}
