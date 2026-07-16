package ucu.retojulio2026.talent.education;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "education")
public class Education {

    @Id
    @Column(name = "education_id", length = 12, updatable = false, nullable = false)
    private String education_id;

    @NotNull(message = "El perfilAlumnoId es obligatorio")
    @Column(name = "perfil_alumno_id", nullable = false)
    private String studentProfileId;

    @NotNull(message = "El titulo es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DegreeLevel degreeLevel;

    @NotBlank(message = "La carrera es obligatoria")
    @Column(nullable = false)
    private String degreeId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate startDate;

    @Column(name = "fecha_fin")
    private LocalDate endDate;

    public enum DegreeLevel {
        TECNICATURA,
        LICENCIATURA,
        GRADO,
        POSGRADO,
        DOCTORADO
    }

    @PrePersist
    protected void assignEducationId() {
        if (this.education_id == null) {
            this.education_id = NanoIdGenerator.generate();
        }
    }
}
