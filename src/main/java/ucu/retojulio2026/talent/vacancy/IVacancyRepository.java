package ucu.retojulio2026.talent.vacancy;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IVacancyRepository extends JpaRepository<Vacancy, String> {
    List<Vacancy> findByStatus(VacancyStatus status);
    List<Vacancy> findByCompanyId(String companyId);
    List<Vacancy> findByAreaId(String areaId);
    List<Vacancy> findByModality(Modality modality);
    List<Vacancy> findByLocation(Departamento location);
}
