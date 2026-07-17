package ucu.retojulio2026.talent.universityregistry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UniversityRegistryRepository extends JpaRepository<UniversityRegistry, String> {
}
