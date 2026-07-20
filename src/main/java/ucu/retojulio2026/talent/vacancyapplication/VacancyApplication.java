package ucu.retojulio2026.talent.vacancyapplication;

import jakarta.persistence.*;
import lombok.*;
import ucu.retojulio2026.talent.common.NanoIdGenerator;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "vacancy_application", uniqueConstraints = @UniqueConstraint(
        name = "uq_vacancy_application_vacancy_student",
        columnNames = {"vacancy_id", "student_profile_id"}))
public class VacancyApplication {

    @Id
    @Column(name = "vacancy_application_id", length = 12, updatable = false, nullable = false)
    private String vacancyApplicationId;

    @Column(name = "vacancy_id", length = 12, nullable = false)
    private String vacancyId;

    @Column(name = "student_profile_id", length = 12, nullable = false)
    private String studentProfileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private VacancyApplicationStatus status;

    @Column(name = "applied_at", nullable = false)
    private LocalDate appliedAt;

    @PrePersist
    protected void assignId() {
        if (this.vacancyApplicationId == null) {
            this.vacancyApplicationId = NanoIdGenerator.generate();
        }
    }
}
