package ucu.retojulio2026.talent.Vacancy;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IVacancyRepository extends JpaRepository<Vacancy, Long> {
    List<Vacancy> findByStatus(JobStatus status);
}
