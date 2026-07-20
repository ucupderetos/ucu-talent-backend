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

    @Id
    @Column(name = "company_id", length = 12, updatable = false, nullable = false)
    private String companyId;

    @Column(name = "name", nullable = false)
    private String name;

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

}
