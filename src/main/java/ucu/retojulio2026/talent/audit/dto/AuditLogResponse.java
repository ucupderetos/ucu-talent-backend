package ucu.retojulio2026.talent.audit.dto;

import java.time.LocalDateTime;

import ucu.retojulio2026.talent.user.Role;

public record AuditLogResponse(
        String auditId,
        String traceId,
        String actorUserId,
        String actorEmail,
        Role actorRole,
        String module,
        String action,
        String entityId,
        String outcome,
        String message,
        Object detail,
        LocalDateTime createdAt
) {
}