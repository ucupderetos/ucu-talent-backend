package ucu.retojulio2026.talent.universityregistry;

import jakarta.persistence.*;
import lombok.*;
import ucu.retojulio2026.talent.common.DocumentType;
import ucu.retojulio2026.talent.common.NanoIdGenerator;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name = "university_registry")
public class UniversityRegistry {

    @Id
    @Column(name = "university_registry_id", length = 12, updatable = false, nullable = false)
    private String universityRegistryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", length = 20, nullable = false)
    private DocumentType documentType;

    @Column(name = "document_number", length = 20, nullable = false)
    private String documentNumber;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String surname;

    @PrePersist
    protected void assignId() {
        if (this.universityRegistryId == null) {
            this.universityRegistryId = NanoIdGenerator.generate();
        }
    }

}
