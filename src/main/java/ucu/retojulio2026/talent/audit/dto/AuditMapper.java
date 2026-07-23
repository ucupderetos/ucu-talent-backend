package ucu.retojulio2026.talent.audit.dto;

import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import ucu.retojulio2026.talent.audit.AuditLog;

@Component
public class AuditMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuditLogResponse toResponse(AuditLog entity) {
        return new AuditLogResponse(
                entity.getAuditId(),
                entity.getTraceId(),
                entity.getActorUserId(),
                entity.getActorEmail(),
                entity.getActorRole(),
                entity.getModule(),
                entity.getAction(),
                entity.getEntityId(),
                entity.getOutcome(),
                entity.getMessage(),
                parseDetail(entity.getDetail()),
                entity.getCreatedAt()
        );
    }

    private Object parseDetail(String detail) {
        if (detail == null) {
            return null;
        }
        try {
            return objectMapper.readValue(detail, Object.class);
        } catch (Exception ex) {
            return detail;
        }
    }
}