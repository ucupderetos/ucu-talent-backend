package ucu.retojulio2026.talent.studentprofile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ucu.retojulio2026.talent.common.DocumentType;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, String> {

    boolean existsByDocumentTypeAndDocumentNumber(DocumentType documentType, String documentNumber);

    boolean existsByCvFile(String cvFile);
}
