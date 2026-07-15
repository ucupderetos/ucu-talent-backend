package ucu.retojulio2026.talent.workexperience;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
    private String id;

    @NotBlank(message = "El perfilAlumnoId es obligatorio")
    @Column(name = "perfil_alumno_id", nullable = false)
    private String perfilAlumnoId;

    @NotBlank(message = "La empresa es obligatoria")
    @Column(nullable = false)
    private String empresa;

    @NotBlank(message = "El puesto es obligatorio")
    @Column(nullable = false)
    private String puesto;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(columnDefinition = "TEXT")
    private String descripcion;
}
