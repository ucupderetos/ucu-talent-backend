package ucu.retojulio2026.talent.studentprofile;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

//Lombok para no tener que generar los Getters, Setters y Constructores básicos.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name = "student_profile")
public class StudentProfile {

    // PK compartida con el User dueño: siempre igual a userId, la asigna
    // StudentProfileMapper al crear (nunca se genera un id nuevo para esta entidad).
    @Id
    @Column(name = "student_profile_id", length = 12, updatable = false, nullable = false)
    private String studentProfileId;

    @Column(name = "user_id", length = 12, nullable = false, unique = true)
    private String userId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "skills", columnDefinition = "jsonb")
    private List<String> skills;

}
