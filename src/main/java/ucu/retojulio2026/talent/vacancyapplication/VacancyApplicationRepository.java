package ucu.retojulio2026.talent.vacancyapplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VacancyApplicationRepository extends JpaRepository<VacancyApplication, String> {

    List<VacancyApplication> findByVacancyId(String vacancyId);

    List<VacancyApplication> findByStudentProfileId(String studentProfileId);

    List<VacancyApplication> findByStatus(VacancyApplicationStatus status);

    boolean existsByVacancyIdAndStudentProfileId(String vacancyId, String studentProfileId);
}
