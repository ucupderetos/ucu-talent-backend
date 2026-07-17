package ucu.retojulio2026.talent.vacancyapplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VacancyApplicationRepository extends JpaRepository<VacancyApplication, String> {
}
