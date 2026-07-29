package ucu.retojulio2026.talent.vacancyapplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
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

    List<VacancyApplication> findByVacancyId(String vacancyId);

    List<VacancyApplication> findByStudentProfileId(String studentProfileId);

    List<VacancyApplication> findByStatus(VacancyApplicationStatus status);

    boolean existsByVacancyIdAndStudentProfileId(String vacancyId, String studentProfileId);

    boolean existsByVacancyId(String vacancyId);

    long countByStatus(VacancyApplicationStatus status);
}
