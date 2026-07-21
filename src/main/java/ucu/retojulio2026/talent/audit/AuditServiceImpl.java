package ucu.retojulio2026.talent.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import ucu.retojulio2026.talent.user.Role;

@Service
public class AuditServiceImpl implements AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditServiceImpl.class);

    private final AuditRepository auditRepository;

    public AuditServiceImpl(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Override
    @Async("taskExecutor")
    public void saveAuditLog(String traceId, String actorUserId, String actorEmail, Role actorRole,
                              String module, String action, String entityId,
                              String outcome, String message, String detail) {
        try {
            AuditLog entry = AuditLog.builder()
                    .traceId(traceId)
                    .actorUserId(actorUserId)
                    .actorEmail(actorEmail)
                    .actorRole(actorRole)
                    .module(module)
                    .action(action)
                    .entityId(entityId)
                    .outcome(outcome)
                    .message(message)
                    .detail(detail)
                    .build();
            auditRepository.save(entry);
        } catch (Exception ex) {
            log.error("No se pudo persistir el audit log para module={} action={} entityId={}",
                    module, action, entityId, ex);
        }
    }
}
