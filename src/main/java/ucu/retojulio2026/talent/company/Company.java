package ucu.retojulio2026.talent.company;

import jakarta.persistence.*;
import lombok.*;
import ucu.retojulio2026.talent.common.Department;
import ucu.retojulio2026.talent.common.NanoIdGenerator;

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

    @Column(name = "user_id", length = 12, nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private String industry;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "web_url", nullable = false)
    private String webUrl;

    @Column(name = "linkedin_url", nullable = false)
    private String linkedinUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "location", nullable = false)
    private Department location;

    @Column(name = "approved", nullable = false)
    private Boolean approved;

    @PrePersist
    protected void assignId() {
        if (this.companyId == null) {
            this.companyId = NanoIdGenerator.generate();
        }
    }

}
