package ucu.retojulio2026.talent.mail;

import jakarta.persistence.*;
import lombok.*;
import ucu.retojulio2026.talent.common.NanoIdGenerator;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "mail_template")
public class MailTemplate {

    @Id
    @Column(name = "mail_template_id", length = 12, updatable = false, nullable = false)
    private String mailTemplateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, length = 30, unique = true)
    private MailTemplateCode code;

    @Column(name = "subject", nullable = false, length = 200)
    private String subject;

    @Column(name = "body", columnDefinition = "TEXT", nullable = false)
    private String body;

    @PrePersist
    protected void assignId() {
        if (this.mailTemplateId == null) {
            this.mailTemplateId = NanoIdGenerator.generate();
        }
    }
}
