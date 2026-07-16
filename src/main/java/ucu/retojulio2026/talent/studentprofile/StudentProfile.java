package ucu.retojulio2026.talent.studentprofile;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ucu.retojulio2026.talent.common.NanoIdGenerator;

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

    @Id
    @Column(name = "student_profile_id", length = 12, updatable = false, nullable = false)
    private String studentProfileId;

    @Column(name = "user_id", length = 12, nullable = false, unique = true)
    private String userId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "skills", columnDefinition = "jsonb")
    private List<String> skills;

    @PrePersist
    protected void assignId() {
        if (this.studentProfileId == null) {
            this.studentProfileId = NanoIdGenerator.generate();
        }
    }

}
