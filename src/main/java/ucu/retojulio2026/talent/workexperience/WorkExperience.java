package ucu.retojulio2026.talent.workexperience;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ucu.retojulio2026.talent.common.NanoIdGenerator;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "work_experience")
public class WorkExperience {

    @Id
    @Column(name = "work_experience_id", length = 12, updatable = false, nullable = false)
    private String workExperienceId;

    @NotBlank(message = "El studentProfileId es obligatorio")
    @Column(name = "student_profile_id", nullable = false)
    private String studentProfileId;

    @Column(name = "company")
    private String company;

    @Column(name = "position")
    private String position;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @PrePersist
    protected void assignId() {
        if (this.workExperienceId == null) {
            this.workExperienceId = NanoIdGenerator.generate();
        }
    }
}
