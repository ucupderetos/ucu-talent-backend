package ucu.retojulio2026.talent.studentprofile;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ucu.retojulio2026.talent.common.DocumentType;

import java.time.LocalDateTime;
import java.util.List;

//Lombok para no tener que generar los Getters, Setters y Constructores básicos.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name = "student_profile", uniqueConstraints = @UniqueConstraint(
        name = "uq_student_profile_document",
        columnNames = {"document_type", "document_number"}))
public class StudentProfile {

    @Id
    @Column(name = "student_profile_id", length = 12, updatable = false, nullable = false)
    private String studentProfileId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String surname;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", length = 20, nullable = false)
    private DocumentType documentType;

    @Column(name = "document_number", length = 20, nullable = false)
    private String documentNumber;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "skills", columnDefinition = "jsonb")
    private List<String> skills;

    @Column(name = "description")
    private String description;

    @Column(name = "cv_file", length = 255, nullable = true)
    private String cvFile;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "admin_comment")
    private String adminComment;

}
