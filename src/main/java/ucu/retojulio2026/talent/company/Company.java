package ucu.retojulio2026.talent.company;

import jakarta.persistence.*;
import lombok.*;
import ucu.retojulio2026.talent.common.Department;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name = "company")
public class Company {

    // PK compartida con el User dueño: siempre igual a userId, la asigna CompanyMapper
    // al crear (nunca se genera un id nuevo para esta entidad).
    @Id
    @Column(name = "company_id", length = 12, updatable = false, nullable = false)
    private String companyId;

    @Column(name = "user_id", length = 12, nullable = false, unique = true)
    private String userId;

    // Nullable: al crearse junto con el User (rol EMPRESA) todavia no hay estos datos;
    // se completan despues con un PUT /company/{id}.
    @Column
    private String industry;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "web_url")
    private String webUrl;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "location")
    private Department location;

    @Column(name = "approved", nullable = false)
    private Boolean approved;

}
