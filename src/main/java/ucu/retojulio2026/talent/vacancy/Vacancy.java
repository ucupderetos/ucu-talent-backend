package ucu.retojulio2026.talent.vacancy;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ucu.retojulio2026.talent.common.NanoIdGenerator;

import java.time.LocalDate;

@Entity
@Table(name = "\"vacancy\"") // Puesto
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Vacancy {

    @Id
    @Column(name = "vacancy_id", length = 12, updatable = false, nullable = false)
    private String vacancyId;

    //@ManyToOne(fetch = FetchType.LAZY, optional = false)
    //@JoinColumn(name = "company_id", nullable = false)
    //private Company company;

    //@ManyToOne(fetch = FetchType.LAZY, optional = false)
    //@JoinColumn(name = "area_id", nullable = false)
    //private Area area;

    @CreationTimestamp
    @Column(name = "publication_date", updatable = false, nullable = false)
    private LocalDate publicationDate;

    @Column(name = "closing_date")
    private LocalDate closingDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Departamento locality;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Modality modality;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobStatus status;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "contract_type", length = 80)
    private String contractType;

    @Column(name = "salary_range", length = 80)
    private String salaryRange;

    @PrePersist
    protected void assignId() {
        if (this.vacancyId == null) {
            this.vacancyId = NanoIdGenerator.generate();
        }
    }
}
