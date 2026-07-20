package ucu.retojulio2026.talent.audit;

import com.google.api.client.util.DateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import ucu.retojulio2026.talent.common.NanoIdGenerator;
import ucu.retojulio2026.talent.user.Role;

// @Immutable: le dice a Hibernate que esta entidad nunca se hace UPDATE,
// solo INSERT. Un registro de auditoria no deberia poder editarse.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
@Immutable
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @Column(name = "audit_id", length = 12, updatable = false, nullable = false)
    private String auditId;

    // Correlaciona todos los logs (de aplicacion y de auditoria) de un mismo
    // request (ver MdcTaskDecorator)
    @Column(name = "trace_id", length = 36, updatable = false)
    private String traceId;

    // Denormalizado a proposito: sin FK a "user". Si el usuario se borra
    // despues, el log conserva quien hizo la accion en su momento.
    @Column(name = "actor_user_id", length = 12, nullable = false, updatable = false)
    private String actorUserId;

    @Column(name = "actor_email", nullable = false, updatable = false)
    private String actorEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_role", nullable = false, length = 20, updatable = false)
    private Role actorRole;

    @Column(nullable = false, length = 50, updatable = false)
    private String module;

    @Column(nullable = false, length = 100, updatable = false)
    private String action;

    @Column(name = "entity_id", length = 50, updatable = false)
    private String entityId;

    @Column(nullable = false, length = 20, updatable = false)
    private String outcome; // SUCCESS | ERROR

    @Column(nullable = false, columnDefinition = "text", updatable = false)
    private String message;

    // JSON con los argumentos del metodo (sanitizado, sin password/passwordHash)
    // en SUCCESS; stacktrace truncado en ERROR.
    @Column(columnDefinition = "text", updatable = false)
    private String detail;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void assignDefault() {
        if (this.auditId == null) {
            this.auditId = NanoIdGenerator.generate();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now(ZoneId.of("America/Montevideo")); // Así tiene la hora y fecha correcta
        }
    }
}
