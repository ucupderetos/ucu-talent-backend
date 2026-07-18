package ucu.retojulio2026.talent.vacancy;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ucu.retojulio2026.talent.area.Area;
import ucu.retojulio2026.talent.common.NanoIdGenerator;
import ucu.retojulio2026.talent.company.Company;

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

    @Column(name = "company_id", length = 12, updatable = false, nullable = false)
    private String companyId;

    @Column(name = "area_id", length = 12, updatable = false, nullable = true)
    private String areaId;

    @CreationTimestamp
    @Column(name = "publication_date", updatable = false, nullable = false)
    private LocalDate publicationDate;

    @Column(name = "closing_date")
    private LocalDate closingDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Departamento location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Modality modality;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VacancyStatus status;

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
